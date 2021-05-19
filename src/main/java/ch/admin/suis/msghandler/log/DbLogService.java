/*
 * $Id$
 *
 * Copyright (C) 2006 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package ch.admin.suis.msghandler.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import ch.admin.suis.msghandler.util.DateUtils;

/**
 * <p> The implementation of the
 * <code>LogService</code> interface that uses the HSQL to store the status of the messages. The DB files are located in
 * the directory specified by the
 * <code>base</code> property. The DB entries are periodically cleaned up. The number of days the entries are held in
 * the DB is determined by the
 * <code>maxAge</code> property. </p>
 *
 * <p> This class is not thread-safe, but intended to be used as a singleton. So, additional measures need to be taken
 * if the synchronozation is required. </p>
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class DbLogService implements LogService {

  /**
   * logger
   */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DbLogService.class.getName());

  private static final String DB_FILE_NAME_PREFIX = "msghandler_log";

  private static final int POS_PARTICIPANT_ID = 0;

  private static final int POS_FILENAME = 1;

  private static final int POS_MESSAGE_ID = 2;

  private static final int POS_SENT_DATE = 3;

  private static final int POS_RECEIVED_DATE = 4;

  private static final int POS_LOG_STATUS = 5;

  private static final int POS_MESSAGE_SOURCE = 6;

  private Connection connection;

  private QueryRunner runner;

  private String base;

  private int maxAge;

  private boolean resend;

  private static final String CREATE_TABLE_STATEMENT = "CREATE CACHED TABLE status ("
          + "participant_id VARCHAR(255) NOT NULL, "
          + "filename VARCHAR(255) NOT NULL, "
          + "message_id VARCHAR(255), "
          + "sent_date DATETIME, "
          + "received_date DATETIME, "
          + "status TINYINT NOT NULL, "
          + "source TINYINT NOT NULL, "
          + "UNIQUE (participant_id, filename)" + ")";

  private static final String SELECT_STATEMENT = "SELECT status FROM status WHERE participant_id = ? AND filename = ?";

  private static final String SELECT_SENT_DATE_STATEMENT = "SELECT sent_date FROM status WHERE message_id = ?";

  private static final String SELECT_SOURCE_STATEMENT = "SELECT source FROM status WHERE message_id = ?";

  private static final String SELECT_MESSAGE_ID_ALL_STATEMENT = "SELECT DISTINCT message_id FROM status WHERE status = ?";

  private static final String SELECT_FILENAME_STATEMENT = "SELECT DISTINCT filename FROM status WHERE message_id = ?";

  private static final String SELECT_ALL_STATEMENT = "SELECT * FROM status";

  private static final String INSERT_STATEMENT = "INSERT INTO status(participant_id, filename, status, source) "
          + "VALUES(?, ?, ?, ?)";

  private static final String UPDATE_SENT_STATEMENT = "UPDATE status SET "
          + "status = ?, sent_date = ?, received_date = NULL, message_id = ?, source = ? WHERE participant_id = ? AND filename = ?";

  private static final String UPDATE_RECEIVED_STATEMENT = "UPDATE status SET "
          + "status = ?, received_date = ? WHERE message_id = ?";

  private static final String CLEANUP_STATEMENT = "DELETE FROM status WHERE "
      + "(received_date IS NULL AND DATEDIFF('dd', sent_date, CURRENT_TIMESTAMP) >= ?) OR "
      + "DATEDIFF('dd', received_date, CURRENT_TIMESTAMP) >= ?";

  private static final String SELECT_AGED_STATEMENT = "SELECT message_id FROM status WHERE "
      + "(received_date IS NULL AND DATEDIFF('dd', sent_date, CURRENT_TIMESTAMP) >= ?) OR "
      + "DATEDIFF('dd', received_date, CURRENT_TIMESTAMP) >= ?";

  private static final String DUMP_LINE = "{0},{1},{2},{3},{4},{5}" + System.getProperty("line.separator");

  private static final String CHECKPOINT_DEFRAG_STATEMENT = "CHECKPOINT DEFRAG";

  private static final String SET_LOGSIZE_STATEMENT = "SET FILES LOG SIZE 1";

  /**
   * Sets the path location where the database files are located. This is the absolute path.
   *
   * @param logBase
   */
  public void setBase(String logBase) {
    base = logBase;
  }

  /**
   * Sets the number of days, the log database entries are held in the file before they are deleted.
   *
   * @param maxAge
   */
  public void setMaxAge(int maxAge) {
    this.maxAge = maxAge;
  }

  /**
   * <p> Sets the flag whether the once sent files can be sent again. By default, if this method was never called, the
   * files are not resend.
   *
   * <p> MANTIS 3301
   *
   * @param resend
   * <code>true</code> if the files are allowed to be sent again and
   * <code>false</code> otherwise
   */
  public void setResend(boolean resend) {
    this.resend = resend;
  }

  /**
   * Initializes the log service by creating the database and the log table and getting the SQL connection. This method
   * loads the HSQL JDBC driver and gets the connection to the database. After that an attempt is made to clean up the
   * old records. If the DB table does not exist, it is created. If the JDBC driver cannot be loaded, this method logs
   * atr fatal level and returns.
   *
   * @throws SQLException if the JDBC connection or the DB table cannot be created
   */
  public void init() throws SQLException {
    // load the driver
    try{
      Class.forName("org.hsqldb.jdbcDriver");
    }
    catch(ClassNotFoundException e){
      LOG.fatal("cannot find the HSQL driver class");
      return;
    }

    // get the connection from the manager
    connection = DriverManager.getConnection("jdbc:hsqldb:" + base
            + File.separator + DB_FILE_NAME_PREFIX, // filenames
            "SA", // username
            ""); // password

    runner = new QueryRunner();

    // initialize the log size to 1MB to possibly prevent a very slow start if the MH was stopped before
    // a DB checkpoint had been reached (default is 200 MB which is too much)
    runner.update(connection, SET_LOGSIZE_STATEMENT);

    // check if the status table already exists
    try{
      // we run the cleanup statement; if it fails, then the database file does not probably exist
      cleanup();
    }
    catch(SQLException e){
      LOG.error(e.getMessage());
      // create the table if it does not exist
      LOG.debug("creating the status table");
      runner.update(connection, CREATE_TABLE_STATEMENT);
      LOG.info("the status table created");
    }

  }

  /**
   * Removes the aged records.
   *
   * @throws SQLException if the operation cannot be performed.
   */
  private void cleanup() throws SQLException
  {
    LOG.info("starting to remove aged records from the log table");
    int count = runner.update(connection, CLEANUP_STATEMENT, new Object[]{
              maxAge, maxAge});
    LOG.info(count + " aged records removed from the log table while performing cleanup");

    // compact the DB
    LOG.info("compacting DB files");
    runner.update(connection, CHECKPOINT_DEFRAG_STATEMENT);
    LOG.info("DB files compacted");
  }

  /*
   * (non-Javadoc) @see ch.admin.suis.msghandler.log.LogService#removeAged()
   */
  @Override
  public Collection<String> removeAged() {
    try{
			ArrayList<Object[]> rowSet = (ArrayList<Object[]>) runner.query(
					connection, SELECT_AGED_STATEMENT, new ArrayListHandler(), maxAge,
					maxAge);

      List<String> result = new ArrayList<>(rowSet.size());

      // remove the records
      cleanup();

      // add the message ids
      for(Object[] row : rowSet) {
        // we have only one element in the array - the sent date and it is a
        // java.lang.String
        result.add((String) row[0]);
      }

      return result;
    }
    catch(SQLException e){
      LOG.error("cannot remove the aged records from the log table", e);
      return Collections.emptyList();
    }
  }

  @Override
  public boolean setSending(Mode source, List<String> participantIds, String filename) throws LogServiceException {
    boolean retVal = false;
    for(String participantId : participantIds){
      retVal = setSending(source, participantId, filename);
    }
    return retVal;
  }

  /*
   * (non-Javadoc)
   *
   * @see ch.admin.suis.msghandler.log.LogService#setSending(java.lang.String, java.lang.String)
   */
  @Override
  public boolean setSending(Mode source, String participantId, String filename)
          throws LogServiceException {
    try{
      // select to see if we have this record already
      // we receive a java.lang.Integer here because
      // the status is defined as TINYINT
			Integer value = runner.query(connection, SELECT_STATEMENT,
					new ScalarHandler<Integer>(), participantId, filename);

      // insert, if not
      if(null == value) {
        runner.update(connection, INSERT_STATEMENT, new Object[]{
                  participantId, filename, LogStatus.SENDING.getCode(), source.getCode()});
      }
      else {
        // some record is already there
        if (LogStatus.ERROR.getCode() == value)
        {
          // rewrite with the status "sending"
          runner.update(connection, UPDATE_SENT_STATEMENT, new Object[]{
                    LogStatus.SENDING.getCode(),
                    new Timestamp(System.currentTimeMillis()), null,
                    source.getCode(),
                    participantId, filename});
        }
        else {
          // cannot change the status if the special flag is not set
          return resend;
        }
      }
    }
    catch(SQLException e){
      LOG.error("DB error while querying the status table: " + e.getMessage());
      throw new LogServiceException(e);
    }

    return true;
  }

  @Override
  public void setForwarded(Mode source, List<String> participantIds, String filename, String messageId)
          throws LogServiceException {
    for(String participantId : participantIds) {
      setForwarded(source, participantId, filename, messageId);
    }
  }

  /**
   * Sets the message status to
   * <code>FORWARDED</code>.
   *
   * @see ch.admin.suis.msghandler.log.LogService#setForwarded(Mode, java.lang.String, java.lang.String,
   * java.lang.String)
   */
  @Override
  public void setForwarded(Mode source, String participantId, String filename, String messageId)
          throws LogServiceException {
    try{
      // update with the new status
      runner.update(connection, UPDATE_SENT_STATEMENT, new Object[]{
        LogStatus.FORWARDED.getCode(), new Timestamp(System.currentTimeMillis()),
        messageId, source.getCode(), participantId, filename});
    }
    catch(SQLException e){
      LOG.error("DB error while querying the status table: " + e.getMessage());
      throw new LogServiceException(e);
    }

  }

  /*
   * (non-Javadoc) @see ch.admin.suis.msghandler.log.LogService#getSentDate(java.lang.String)
   */
  @Override
  public Date getSentDate(String messageId) throws LogServiceException {
    try{
      ArrayList<Object[]> rowSet = (ArrayList<Object[]>) runner.query(connection, SELECT_SENT_DATE_STATEMENT,
					new ArrayListHandler(), messageId);

      List<Date> result = new ArrayList<Date>(rowSet.size());
      for(Object[] row : rowSet) {
        // we have only one element in the array - the sent date and it is a
        // java.util.Date
        result.add((Date) row[0]);
      }

      if(result.isEmpty()) {
        // nothing found
        return null;
      }
      else {
        // otherwise return the first encountered value
        return result.get(0);
      }
    }
    catch(SQLException e){
      throw new LogServiceException(e);
    }
  }

  /**
   * Closes the SQL connection and shuts down the HBSQL database. This method MUST be called before quitting the
   * program.
   */
  public void destroy() {
    try{
      if(null != runner) {
        runner.update(connection, "SHUTDOWN");
        LOG.debug("shutdown statement issued for the log service");
      }

      DbUtils.close(connection);
      LOG.debug("log service stopped");
    } catch (SQLException e)
    {
      // ignore
      LOG.error("cannot close the DB connection while stopping the log service: " + e.getMessage());
    }
  }

  /**
   * Creates the CSV-file in the directory in which the database files are located. If the service was not initialized,
   * this method silently returns.
   *
   */
	public void dump() throws UnsupportedEncodingException {
    if(null == connection) {
      // this service was not initialized
      LOG.error("cannot create the DB dump file: the log service was not initialized");
      return;
    }

    File dumpFile = new File(base, "dump.csv"); // TODO make this configurable
    BufferedWriter writer;
    try{
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(dumpFile), StandardCharsets.ISO_8859_1));
    }
    catch(FileNotFoundException e){
      LOG.fatal("cannot create the DB dump file " + dumpFile.getAbsolutePath());
      return;
    }

    try{
      // read all the available records from the DB
      ArrayList<Object[]> rowSet = (ArrayList<Object[]>) runner.query(connection,
              "select * from status", new ArrayListHandler());

      for(Object[] row : rowSet) {
        for(int i = 0; i < row.length; i++) {
          // replace the null values with empty strings
          row[i] = null == row[i] ? "" : row[i];
        }
        writer.write(MessageFormat.format(DUMP_LINE, row));
      }

    }
    catch(SQLException e){
      LOG.fatal("cannot read from the status table: " + e.getMessage());
    }
    catch(IOException e){
      LOG.fatal("cannot write to the dump file: " + e.getMessage());
    }
    finally{
      if(null != writer) {
        try{
          writer.close();
        }
        catch(IOException e){
          // ignore
        }
      }
    }


  }

  /*
   * (non-Javadoc) @see ch.admin.suis.msghandler.log.LogService#getSentMessages()
   */
  @Override
  public List<String> getSentMessages() throws LogServiceException {
    List<String> result = new ArrayList<String>();
    result.addAll(getMessages(LogStatus.SENT));
    result.addAll(getMessages(LogStatus.FORWARDED));
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see ch.admin.suis.msghandler.log.LogService#setReceived(java.lang.String, java.util.Date)
   */
  @Override
  public void setStatusChange(String messageid, Date changeDate, LogStatus status)
          throws LogServiceException {
    try{
      // update with the new status
      runner.update(connection, UPDATE_RECEIVED_STATEMENT, new Object[]{
                status.getCode(), new Timestamp(changeDate.getTime()),
                messageid});
    }
    catch(SQLException e){
      LOG.error("DB error while querying the status table: "
              + e.getMessage());
      throw new LogServiceException(e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see ch.admin.suis.msghandler.log.LogService#getFiles(java.lang.String)
   */
  @Override
  public List<String> getFiles(String messageId) throws LogServiceException {
    try{
			ArrayList<Object[]> rowSet = (ArrayList<Object[]>) runner.query(
					connection, SELECT_FILENAME_STATEMENT, new ArrayListHandler(),
					messageId);

      List<String> result = new ArrayList<String>(rowSet.size());
      for(Object[] row : rowSet) {
        // we have only one element in the array - the filename and it is a
        // string
        result.add((String) row[0]);
      }

      return result;
    }
    catch(SQLException e){
      throw new LogServiceException(e);
    }
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public List<DBLogEntry> getAllEntries() throws LogServiceException {
    try{
			ArrayList<Object[]> rowSet = (ArrayList<Object[]>) runner
					.query(connection, SELECT_ALL_STATEMENT, new ArrayListHandler());

      List<DBLogEntry> results = new ArrayList<DBLogEntry>(rowSet.size());
      for(Object[] row : rowSet) {
        DBLogEntry logEntry = parseDBLogEntry(row);
        results.add(logEntry);
      }

      return results;
    }
    catch(SQLException e){
      throw new LogServiceException(e);
    }
  }

  private DBLogEntry parseDBLogEntry(Object[] row) {
    DBLogEntry logEntry = new DBLogEntry();
    if(row[POS_PARTICIPANT_ID] != null) {
      logEntry.setRecipientId(row[POS_PARTICIPANT_ID].toString());
    }
    if(row[POS_FILENAME] != null) {
      logEntry.setFilename(row[POS_FILENAME].toString());
    }
    if(row[POS_MESSAGE_ID] != null) {
      logEntry.setMessageId(row[POS_MESSAGE_ID].toString());
    }
    if(row[POS_SENT_DATE] != null) {
      logEntry.setSentDate(DateUtils.dateToXsdDateTime(new Date(((Timestamp) row[POS_SENT_DATE]).getTime())));
    }
    if(row[POS_RECEIVED_DATE] != null) {
      logEntry.setReceivedDate(DateUtils.dateToXsdDateTime(new Date(((Timestamp) row[POS_RECEIVED_DATE]).getTime())));
    }
    if(row[POS_LOG_STATUS] != null) {
      logEntry.setState(LogStatus.fromInt((Integer) row[POS_LOG_STATUS]));
    }
    if(row[POS_MESSAGE_SOURCE] != null) {
      logEntry.setMode(Mode.fromInt((Integer) row[POS_MESSAGE_SOURCE]));
    }
    return logEntry;
  }

  /*
   * (non-Javadoc) @see ch.admin.suis.msghandler.log.LogService#getMessages(ch.admin.suis.msghandler.log.LogStatus)
   */
  @Override
  public List<String> getMessages(LogStatus status) throws LogServiceException {
    try{
			ArrayList<Object[]> rowSet = (ArrayList<Object[]>) runner.query(
					connection, SELECT_MESSAGE_ID_ALL_STATEMENT, new ArrayListHandler(),
					status.getCode());

      List<String> result = new ArrayList<String>(rowSet.size());
      for(Object[] row : rowSet) {
        // we have only one element in the array - the message ID and it is a
        // string
        result.add((String) row[0]);
      }

      return result;
    }
    catch(SQLException e){
      throw new LogServiceException(e);
    }
  }

  /*
   * (non-Javadoc) @see ch.admin.suis.msghandler.log.LogService#isTransparent(java.lang.String)
   */
  @Override
  public boolean isTransparent(String messageId) throws LogServiceException {
    try{
			ArrayList<Object[]> rowSet = (ArrayList<Object[]>) runner.query(
					connection, SELECT_SOURCE_STATEMENT, new ArrayListHandler(),
					messageId);

      List<Integer> result = new ArrayList<Integer>(rowSet.size());
      for(Object[] row : rowSet) {
        // we have only one element in the array - the message ID and it is a
        // string
        result.add((Integer) row[0]);
      }

      if(result.isEmpty()) {
        // nothing found
        return true;
      }
      else {
        // otherwise return the first encountered value
        return Mode.TRANSP.getCode() == result.get(0);
      }
    }
    catch(SQLException e){
      throw new LogServiceException(e);
    }
  }
}
