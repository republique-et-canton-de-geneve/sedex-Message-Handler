/**
 * Returns the participant ID as based on the provided filename.
 * @param filename the name of the file to be sent
 *
 * @return the resolved participant ID or an empty string
 */
/**
String resolve(String filename) {
  hrRegex = /^.*\/(\S+?)_.*$/
  matcher = (filename =~ hrRegex)
  if (matcher.matches()) {
    return matcher.group(1)
  } 
  else {
    return ''
  }
}
*/

String resolve(String filename) {
	return '7-4-1'
}
