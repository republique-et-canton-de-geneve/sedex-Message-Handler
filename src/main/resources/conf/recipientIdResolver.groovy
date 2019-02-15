import java.util.regex.Matcher

/**
 * This resolver works for eSchKG messages. The sedex ID will be extracted
 * from the filename.
 *
 * @param filename the name of the file to be sent including path
 * @return the resolved Sedex-ID or an empty string
 */
def String resolve(String filename) {
    Matcher matcher = null;
    if (System.getProperty("os.name").startsWith("Windows")) {
        matcher = (filename =~ /^.*\\(\S+?)_.*$/)
    } else {
        // hopefully some sort of Unix. Kept for retrocompatibility purposes.
        matcher = (filename =~ /^.*\/(\S+?)_.*$/)
    }
    if (matcher.matches()) {
        return matcher.group(1)
    }

    // If all else fails: return empty string
    return ''
}

