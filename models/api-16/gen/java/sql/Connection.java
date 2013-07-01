package java.sql;
public interface Connection
  extends java.sql.Wrapper
{
public abstract  void clearWarnings() throws java.sql.SQLException;
public abstract  void close() throws java.sql.SQLException;
public abstract  void commit() throws java.sql.SQLException;
public abstract  java.sql.Statement createStatement() throws java.sql.SQLException;
public abstract  java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws java.sql.SQLException;
public abstract  java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException;
public abstract  boolean getAutoCommit() throws java.sql.SQLException;
public abstract  java.lang.String getCatalog() throws java.sql.SQLException;
public abstract  int getHoldability() throws java.sql.SQLException;
public abstract  java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException;
public abstract  int getTransactionIsolation() throws java.sql.SQLException;
public abstract  java.util.Map<java.lang.String, java.lang.Class<?>> getTypeMap() throws java.sql.SQLException;
public abstract  java.sql.SQLWarning getWarnings() throws java.sql.SQLException;
public abstract  boolean isClosed() throws java.sql.SQLException;
public abstract  boolean isReadOnly() throws java.sql.SQLException;
public abstract  java.lang.String nativeSQL(java.lang.String sql) throws java.sql.SQLException;
public abstract  java.sql.CallableStatement prepareCall(java.lang.String sql) throws java.sql.SQLException;
public abstract  java.sql.CallableStatement prepareCall(java.lang.String sql, int resultSetType, int resultSetConcurrency) throws java.sql.SQLException;
public abstract  java.sql.CallableStatement prepareCall(java.lang.String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException;
public abstract  java.sql.PreparedStatement prepareStatement(java.lang.String sql) throws java.sql.SQLException;
public abstract  java.sql.PreparedStatement prepareStatement(java.lang.String sql, int autoGeneratedKeys) throws java.sql.SQLException;
public abstract  java.sql.PreparedStatement prepareStatement(java.lang.String sql, int[] columnIndexes) throws java.sql.SQLException;
public abstract  java.sql.PreparedStatement prepareStatement(java.lang.String sql, int resultSetType, int resultSetConcurrency) throws java.sql.SQLException;
public abstract  java.sql.PreparedStatement prepareStatement(java.lang.String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException;
public abstract  java.sql.PreparedStatement prepareStatement(java.lang.String sql, java.lang.String[] columnNames) throws java.sql.SQLException;
public abstract  void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException;
public abstract  void rollback() throws java.sql.SQLException;
public abstract  void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException;
public abstract  void setAutoCommit(boolean autoCommit) throws java.sql.SQLException;
public abstract  void setCatalog(java.lang.String catalog) throws java.sql.SQLException;
public abstract  void setHoldability(int holdability) throws java.sql.SQLException;
public abstract  void setReadOnly(boolean readOnly) throws java.sql.SQLException;
public abstract  java.sql.Savepoint setSavepoint() throws java.sql.SQLException;
public abstract  java.sql.Savepoint setSavepoint(java.lang.String name) throws java.sql.SQLException;
public abstract  void setTransactionIsolation(int level) throws java.sql.SQLException;
public abstract  void setTypeMap(java.util.Map<java.lang.String, java.lang.Class<?>> map) throws java.sql.SQLException;
public abstract  java.sql.Clob createClob() throws java.sql.SQLException;
public abstract  java.sql.Blob createBlob() throws java.sql.SQLException;
public abstract  java.sql.NClob createNClob() throws java.sql.SQLException;
public abstract  java.sql.SQLXML createSQLXML() throws java.sql.SQLException;
public abstract  boolean isValid(int timeout) throws java.sql.SQLException;
public abstract  void setClientInfo(java.lang.String name, java.lang.String value) throws java.sql.SQLClientInfoException;
public abstract  void setClientInfo(java.util.Properties properties) throws java.sql.SQLClientInfoException;
public abstract  java.lang.String getClientInfo(java.lang.String name) throws java.sql.SQLException;
public abstract  java.util.Properties getClientInfo() throws java.sql.SQLException;
public abstract  java.sql.Array createArrayOf(java.lang.String typeName, java.lang.Object[] elements) throws java.sql.SQLException;
public abstract  java.sql.Struct createStruct(java.lang.String typeName, java.lang.Object[] attributes) throws java.sql.SQLException;
public static final int TRANSACTION_NONE = 0;
public static final int TRANSACTION_READ_COMMITTED = 2;
public static final int TRANSACTION_READ_UNCOMMITTED = 1;
public static final int TRANSACTION_REPEATABLE_READ = 4;
public static final int TRANSACTION_SERIALIZABLE = 8;
}
