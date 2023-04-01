package me.eglp.gv2.util.base.guild.reminder.timestampParserStuff;

import java.util.Set;

/**
 * An interface to return a set of available language tags supported by a
 * LocaleServiceProvider.
 *
 * @author Masayoshi Okutsu
 */
public interface AvailableLanguageTags {
    /**
     * Returns a set of available language tags of a LocaleServiceProvider.
     * Note that the returned set doesn't contain the language tag for
     * {@code Locale.Root}.
     *
     * @return a Set of available language tags.
     */
    public Set<String> getAvailableLanguageTags();
}
