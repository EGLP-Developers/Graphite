package me.eglp.gv2.util.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import me.eglp.gv2.main.DebugCategory;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.GraphiteDebug;
import me.eglp.gv2.main.GraphiteOption;
import me.eglp.gv2.main.GraphiteSetupException;
import me.eglp.gv2.util.settings.MySQLSettings;
import me.mrletsplay.mrcore.misc.ErroringNullableOptional;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class GraphiteMySQL {
	
	private static final Reflections REFLECTIONS = new Reflections("me.eglp.gv2", Scanners.TypesAnnotated);
	private static final Set<Class<?>> SQL_CLASSES = getSQLClasses0();
	
	private BasicDataSource dataSource;

	private static Set<Class<?>> getSQLClasses0() {
		Set<Class<?>> classes = new HashSet<>(REFLECTIONS.getTypesAnnotatedWith(SQLTable.class));
		classes.addAll(REFLECTIONS.getTypesAnnotatedWith(SQLTables.class));
		return classes;
	}
	
	public GraphiteMySQL() {
		MySQLSettings s = Graphite.getMainBotInfo().getMySQL();
		
		dataSource = new BasicDataSource();
		dataSource.setUrl(s.getURL());
		dataSource.setUsername(s.getUsername());
		dataSource.setPassword(s.getPassword());
		dataSource.setMaxTotal(10);
		dataSource.setDefaultQueryTimeout(10);
		dataSource.setMaxWaitMillis(30000);
		dataSource.setDefaultSchema(s.getDatabase());
		dataSource.setDefaultCatalog(s.getDatabase());
		
		try(Connection c = dataSource.getConnection()) {
			
		}catch(SQLException e) {
			throw new GraphiteSetupException("MySQL connection error", e);
		}
	}
	
	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public TableBuilder createTable(String name) {
		return new TableBuilder(name);
	}
	
	public List<TableBuilder> getTables() {
		List<TableBuilder> tables = new ArrayList<>();
		SQL_CLASSES.forEach(c -> {
			SQLTable[] ts = c.getAnnotationsByType(SQLTable.class);
			for(SQLTable t : ts) {
				TableBuilder b = createTable(t.name())
					.addColumns(t.columns());
				if(!t.collation().isEmpty()) b.collation(t.collation());
				if(!t.charset().isEmpty()) b.charset(t.charset());
				if(!t.guildReference().isEmpty()) b.guildReference(t.guildReference());
				tables.add(b);
			}
		});
		return tables;
	}
	
	public void createTables() {
		getTables().forEach(t -> t.create());
	}
	
	public void run(UnsafeConsumer<Connection> run) {
		if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log("REQUESTED QUERY: " + run);
		try (Connection con = getConnection()){
			if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log(">> ACQUIRED CONNECTION: " + run);
			run.accept(con);
			if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log("<< RELEASE CONNECTION");
		}catch(Exception e) {
			e.printStackTrace();
			GraphiteDebug.log(DebugCategory.MYSQL, e);
		}
	}
	
	public <T> ErroringNullableOptional<T, Exception> run(UnsafeFunction<Connection, T> run) {
		if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log("REQUESTED QUERY: " + run);
		try (Connection con = getConnection()){
			if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log(">> ACQUIRED CONNECTION: " + run);
			var it = ErroringNullableOptional.<T, Exception>ofErroring(run.apply(con));
			if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log("<< RELEASE CONNECTION");
			return it;
		}catch(Exception e) {
			GraphiteDebug.log(DebugCategory.MYSQL, e);
			return ErroringNullableOptional.ofErroring(e);
		}
	}
	
	public <T> ErroringNullableOptional<List<T>, Exception> queryArray(Class<T> resultType, String query, Object... parameters) {
		return Graphite.getMySQL().run(con -> {
			if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log("RUN QUERY: " + query);
			try(PreparedStatement p = con.prepareStatement(query)) {
				if(p.getParameterMetaData().getParameterCount() != parameters.length) throw new FriendlyException("Parameter counts don't match");
				for(int i = 0; i < parameters.length; i++) {
					p.setObject(i + 1, parameters[i]);
				}
				try(ResultSet r = p.executeQuery()) {
					if(Graphite.hasOption(GraphiteOption.MYSQL_DEBUG)) Graphite.log("QUERY DONE: " + query);
					if(resultType.equals(Void.class)) return Collections.emptyList();
					if(r.getMetaData().getColumnCount() > 1) throw new FriendlyException("query() can only handle one return value");
					List<T> list = new ArrayList<>();
					while(r.next()) {
						list.add(resultType.cast(r.getObject(1, resultType)));
					}
					return list;
				}
			}
		});
	}
	
	public <T> ErroringNullableOptional<T, Exception> query(Class<T> resultType, T defaultValue, String query, Object... parameters) {
		ErroringNullableOptional<List<T>, Exception> e = queryArray(resultType, query, parameters);
		if(!e.isPresent()) return ErroringNullableOptional.ofErroring(e.getException());
		List<T> r = e.get();
		if(r.isEmpty()) return ErroringNullableOptional.ofErroring(defaultValue);
		if(r.size() > 1) {
			FriendlyException ex = new FriendlyException("More than one result was returned");
			GraphiteDebug.log(DebugCategory.MYSQL, ex);
			ErroringNullableOptional.ofErroring(ex);
		}
		return ErroringNullableOptional.ofErroring(r.get(0));
	}
	
	public ErroringNullableOptional<?, Exception> query(String query, Object... parameters) {
		return queryArray(Void.class, query, parameters);
	}
	
	public void close() {
		try {
			dataSource.close();
		} catch (SQLException e) {
			GraphiteDebug.log(DebugCategory.MYSQL, e);
		}
	}
	
	@FunctionalInterface
	public static interface UnsafeConsumer<T> {
		
		public void accept(T t) throws Exception;
		
	}
	
	@FunctionalInterface
	public static interface UnsafeFunction<T, U> {
		
		public U apply(T t) throws Exception;
		
	}
	
	public class TableBuilder {
		
		private String name;
		private String charset;
		private String collation;
		private List<String> columns;
		private String guildReference;
		
		public TableBuilder(String name) {
			this.name = name;
			this.columns = new ArrayList<>();
		}
		
		public TableBuilder charset(String charset) {
			this.charset = charset;
			return this;
		}
		
		public TableBuilder collation(String collation) {
			this.collation = collation;
			return this;
		}
		
		public TableBuilder addColumn(String column) {
			this.columns.add(column);
			return this;
		}
		
		public TableBuilder addColumns(String... columns) {
			this.columns.addAll(Arrays.asList(columns));
			return this;
		}
		
		public TableBuilder guildReference(String guildReference) {
			this.guildReference = guildReference;
			return this;
		}
		
		public String createQuery() {
			if(name == null) throw new IllegalStateException("name == null");
			if(columns.isEmpty()) throw new IllegalStateException("No columns");
			
			if(guildReference != null) Graphite.getDataManager().addGuildReference(name, guildReference);
			StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(name);
			query.append("(");
			query.append(columns.stream().collect(Collectors.joining(",")));
			query.append(")");
			if(charset != null) query.append(" CHARACTER SET ").append(charset);
			if(collation != null) query.append(" COLLATE ").append(collation);
			return query.toString();
		}
		
		public void create() {
			query(createQuery()).orElseThrowOther(e -> new FriendlyException("Failed to create table", e));
		}
		
	}
	
}
