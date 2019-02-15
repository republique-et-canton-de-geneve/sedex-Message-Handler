import groovy.sql.Sql

/**
 * Example script for recipient Id resolution using a MySQL database.
 *
 * Returns the participant ID as based on the provided filename.
 * @param filename the name of the file to be sent
 *
 * @return the resolved participant ID or an empty string
 */
String resolve(String filename) {
    customId = filename[0..(filename.indexOf('_') - 1)]

    db = Sql.newInstance(
            'jdbc:mysql://mysql.test.glue.ch:25015/db?autoReconnect=true',
            'user',
            'password',
            'com.mysql.jdbc.Driver'
    )

    def result = ''

    db.query("select PARTICIPANT_ID from LOG_MAPPING where CUSTOM_ID = ${customId}") { rs ->
        if (rs.next()) {
            result = rs.getString(1)
        }
    }

    return result
}
