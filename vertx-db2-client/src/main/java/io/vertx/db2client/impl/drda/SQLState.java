/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl.drda;
/**
	List of error message identifiers.
	This is the set of message identifiers. The message identifier
	also encodes the SQLState as the first five characters.
	StandardExceptions must be created using the static
	StandardException.newException() method calls, passing in a
	field from this class.
	<BR>
	The five character SQL State is obtained from a StandardException
	using the zero-argument StandardException.getSQLState() method.
	<BR>
	The message identifier (ie. the value that matches a field in this class)
	is obtained using the zero-argument StandardException.getMessageId() method.
	<BR>
	Thus if checking for a specific error using a field from this interface
	the correct code is
	<PRE>
		if (se.getMessageId().equals(SQLState.DEADLOCK))
	</PRE>
	<BR>
	A utility static method StandardException.getSQLState(String messageId)
	exists to convert an field from this class into a five character SQLState.
	<P>

	<P>
	The SQL state of an error message dictates the error's severity.
	The severity is determined from the first two characters of the
	state if the state is five characters long, otherwise the state
	is expected to be 7 characters long and the last character determines
	the state. If the state is seven characters long then only the first
	five will be seen by the error reporting code and exception.
	<BR>
	If the state is 9 characters long, the last two characters encode
	an exception category, which Synchronization uses to determine whether
	the error causes REFRESH to halt or to simply skip the failed transaction.
	All 5 and 7 character states default to the ENVIRONMENTAL exception
	category.
	<BR>
	Here is the encoding of the SQL state, broken down by severity.
	<UL>
	<LI> <B> SYSTEM_SEVERITY </B>
		xxxxx.M
		
	<LI> <B> DATABASE_SEVERITY </B>
		xxxxx.D

	<LI> <B> SESSION_SEVERITY </B>
	    08xxx
		xxxxx.C

	<LI> <B> TRANSACTION_SEVERITY </B>
		40xxx or xxxxx.T

	<LI> <B> STATEMENT_SEVERITY </B>
		{2,3}xxxx, 42xxx,  07xxx  or xxxxx.S

	<LI> <B> WARNING_SEVERITY </B>
		01xxx <EM> SQL State rules require that warnings have states starting with 01</EM>

	<LI> <B> NO_APPLICABLE_SEVERITY </B>
		YYxxx (YY means none of the above) or xxxxx.U

	<LI> <B> TRANSIENT exception category </B>
		xxxxx.Y#T (Y can be any of the preceding severities)

	<LI> <B> CONSISTENCY exception category </B>
		xxxxx.Y#C (Y can be any of the preceding severities)

	<LI> <B> ENVIRONMENTAL exception category (the default)</B>
		xxxxx.Y#E (Y can be any of the preceding severities)

	<LI> <B> WRAPPED exception category</B>
		xxxxx.Y#W (Y can be any of the preceding severities)

	</UL>
	<HR>
	<P>
	<B>SQL State ranges</B>
	<UL>
	<LI>Basic Services
	  <UL>
	  <LI> XBCA CacheService
	  <LI> XBCM ClassManager
	  <LI> XBCX	Cryptography
	  <LI> XBM0	Monitor
	  <LI> XBDA Communications
	  <LI> XCY0 Properties
	  </UL>

	<LI>Connectivity
	  <UL>
	  <LI> 08XXX Connection Exceptions
	  </UL>


	<LI>Language
	  <UL>
		<LI> 2200J-2200R for SQL/XML errors (based on SQL/XML[2006]) </LI>
		<LI> 42800-? for compatible DB2 errors
		<LI> 42X00-42Zxx for compilation errors </LI>
		<LI> 46000  for SQLJ errors (for now, leave this range empty) </LI>
		<LI> 38000  SQL3 ranges  </LI>
		<LI> XD00x  Dependency mgr </LI>
		<LI> XMLxx  Misc XML errors not covered by SQL standard </LI>
	  <LI> 
	  </UL>

	<LI>Store
	  <UL>
	  <LI> XSCG0 Conglomerate
	  <LI> XSCH0 Heap
	  </UL>

	<LI>Security
	  <UL>
	  <LI> XK...
	  </UL>

    <LI>Replication
      <UL>
      <LI> XRExx
      </UL>

    <LI>Reserved for IBM Use: XQC00 - XQCZZ
	</UL>
*/

public interface SQLState {

	/*
	** BasicServices
	*/

	/*
	** Monitor
	*/
	String SERVICE_STARTUP_EXCEPTION			= "XBM01.D";
	String SERVICE_MISSING_IMPLEMENTATION		= "XBM02.D";
	String MISSING_PRODUCT_VERSION				= "XBM05.D";
	String SERVICE_WRONG_BOOT_PASSWORD			= "XBM06.D";
    String SERVICE_PROPERTIES_MISSING			= "XBM0A.D";
    String SERVICE_PROPERTIES_EDIT_FAILED       = "XBM0B.D";
    String MISSING_FILE_PRIVILEGE               = "XBM0C.D";
	String SERVICE_BOOT_PASSWORD_TOO_SHORT		= "XBM07.D";
	String MISSING_ENCRYPTION_PROVIDER			= "XBM0G.D";
	String SERVICE_DIRECTORY_CREATE_ERROR		= "XBM0H.D";
	String SERVICE_DIRECTORY_REMOVE_ERROR		= "XBM0I.D";
	String SERVICE_DIRECTORY_EXISTS_ERROR		= "XBM0J.D";
	String PROTOCOL_UNKNOWN						= "XBM0K.D";

	// these were originally ModuleStartupExceptions
	String AUTHENTICATION_NOT_IMPLEMENTED		= "XBM0L.D";
	String AUTHENTICATION_SCHEME_ERROR			= "XBM0M.D";
	String JDBC_DRIVER_REGISTER					= "XBM0N.D";
	String READ_ONLY_SERVICE					= "XBM0P.D";
	String UNABLE_TO_RENAME_FILE				= "XBM0S.D";
	String AMBIGIOUS_PROTOCOL					= "XBM0T.D";

	String REGISTERED_CLASS_NONE				= "XBM0U.S";
	String REGISTERED_CLASS_LINAKGE_ERROR		= "XBM0V.S";
	String REGISTERED_CLASS_INSTANCE_ERROR		= "XBM0W.S";
	String INVALID_LOCALE_DESCRIPTION			= "XBM0X.D";
	String INVALID_COLLATION			        = "XBM03.D";
	String COLLATOR_NOT_FOUND_FOR_LOCALE        = "XBM04.D";
	String SERVICE_DIRECTORY_NOT_IN_BACKUP      = "XBM0Y.D";
	String UNABLE_TO_COPY_FILE_FROM_BACKUP      = "XBM0Z.D";
	String PROPERTY_FILE_NOT_FOUND_IN_BACKUP    = "XBM0Q.D";
	String UNABLE_TO_DELETE_FILE                = "XBM0R.D";
    String INSTANTIATE_STORAGE_FACTORY_ERROR    = "XBM08.D";

	/*
	** Communications
	*/
    String LOGIN_TIMEOUT                        = "XBDA0.C.1";

	/*
	** Upgrade
	*/
	String UPGRADE_UNSUPPORTED				= "XCW00.D";
	// Note: UPGRADE_SPSRECOMPILEFAILED is now in the warnings section.
	
	/*
	** ContextService
	*/
	String CONN_INTERRUPT					= "08000";


	/*
	** ClassManager
	*/
	String GENERATED_CLASS_LINKAGE_ERROR	= "XBCM1.S";
	String GENERATED_CLASS_INSTANCE_ERROR	= "XBCM2.S";
	String GENERATED_CLASS_NO_SUCH_METHOD	= "XBCM3.S";
	String GENERATED_CLASS_LIMIT_EXCEEDED	= "XBCM4.S";

	/*
	** Cryptography
	*/
	String CRYPTO_EXCEPTION				= "XBCX0.S";
	String ILLEGAL_CIPHER_MODE			= "XBCX1.S";
	String ILLEGAL_BP_LENGTH			= "XBCX2.S";
	String NULL_BOOT_PASSWORD			= "XBCX5.S";
	String NON_STRING_BP				= "XBCX6.S";
	String WRONG_PASSWORD_CHANGE_FORMAT = "XBCX7.S";
	String DATABASE_NOT_ENCRYPTED		= "XBCX8.S";
	String DATABASE_READ_ONLY			= "XBCX9.S";
	String WRONG_BOOT_PASSWORD			= "XBCXA.S";
    String ENCRYPTION_BAD_PADDING       = "XBCXB.S";
    String ENCRYPTION_NOSUCH_ALGORITHM  = "XBCXC.S";
    String ENCRYPTION_NOCHANGE_ALGORITHM     = "XBCXD.S";
    String ENCRYPTION_NOCHANGE_PROVIDER = "XBCXE.S";
    String ENCRYPTION_NO_PROVIDER_CLASS = "XBCXF.S";
    String ENCRYPTION_NOT_A_PROVIDER    = "XBCXF.S.1";
    String ENCRYPTION_BAD_PROVIDER      = "XBCXG.S";
    String ENCRYPTION_BAD_ALG_FORMAT    = "XBCXH.S";
    String ENCRYPTION_BAD_FEEDBACKMODE  = "XBCXI.S";
    String ENCRYPTION_BAD_JCE           = "XBCXJ.S";
    String ENCRYPTION_BAD_EXTERNAL_KEY  = "XBCXK.S";
    String ENCRYPTION_UNABLE_KEY_VERIFICATION  = "XBCXL.S";
    String ENCRYPTION_INVALID_EXKEY_LENGTH          = "XBCXM.S";
    String ENCRYPTION_ILLEGAL_EXKEY_CHARS           = "XBCXN.S";
    String ENCRYPTION_PREPARED_XACT_EXIST             = "XBCXO.S";
    String CANNOT_ENCRYPT_READONLY_DATABASE           = "XBCXQ.S";
    String CANNOT_ENCRYPT_LOG_ARCHIVED_DATABASE       = "XBCXS.S";
    String DATABASE_ENCRYPTION_FAILED                 = "XBCXU.S";
    String DIGEST_NO_SUCH_ALGORITHM                   = "XBCXW.S";

	/*
	** Cache Service
	*/
	String OBJECT_EXISTS_IN_CACHE		= "XBCA0.S";

	/*
	** Properties
	*/
	String PROPERTY_INVALID_VALUE		= "XCY00.S";
	String PROPERTY_UNSUPPORTED_CHANGE  = "XCY02.S";
	String PROPERTY_MISSING				= "XCY03.S";
	String PROPERTY_SYNTAX_INVALID		= "XCY04.S";
	String PROPERTY_CANT_UNDO_NATIVE  = "XCY05.S.2";
	String PROPERTY_DBO_LACKS_CREDENTIALS  = "XCY05.S.3";

	/*
	** LockManager
	*/
	String DEADLOCK = "40001";
	String LOCK_TIMEOUT = "40XL1";
    String LOCK_TIMEOUT_LOG = "40XL1.T.1";
    String SELF_DEADLOCK = "40XL2";

	/*
	** Store - access.protocol.Interface statement exceptions
	*/
	String STORE_CONGLOMERATE_DOES_NOT_EXIST                    = "XSAI2.S";
	String STORE_FEATURE_NOT_IMPLEMENTED                        = "XSAI3.S";

	/*
	** Store - access.protocol.Interface RunTimeStatistics property names
	** and values.
	*/
	String STORE_RTS_SCAN_TYPE									= "XSAJ0.U";
	String STORE_RTS_NUM_PAGES_VISITED							= "XSAJ1.U";
	String STORE_RTS_NUM_ROWS_VISITED							= "XSAJ2.U";
	String STORE_RTS_NUM_DELETED_ROWS_VISITED					= "XSAJ3.U";
	String STORE_RTS_NUM_ROWS_QUALIFIED							= "XSAJ4.U";
	String STORE_RTS_NUM_COLUMNS_FETCHED						= "XSAJ5.U";
	String STORE_RTS_COLUMNS_FETCHED_BIT_SET					= "XSAJ6.U";
	String STORE_RTS_TREE_HEIGHT								= "XSAJ7.U";
	String STORE_RTS_SORT_TYPE									= "XSAJ8.U";
	String STORE_RTS_NUM_ROWS_INPUT								= "XSAJA.U";
	String STORE_RTS_NUM_ROWS_OUTPUT							= "XSAJB.U";
	String STORE_RTS_NUM_MERGE_RUNS								= "XSAJC.U";
	String STORE_RTS_MERGE_RUNS_SIZE							= "XSAJD.U";
	String STORE_RTS_ALL										= "XSAJE.U";
	String STORE_RTS_BTREE										= "XSAJF.U";
	String STORE_RTS_HEAP										= "XSAJG.U";
	String STORE_RTS_SORT										= "XSAJH.U";
	String STORE_RTS_EXTERNAL									= "XSAJI.U";
	String STORE_RTS_INTERNAL									= "XSAJJ.U";

	/*
	** Store - access.protocol.XA statement exceptions
	*/
	String STORE_XA_PROTOCOL_VIOLATION                          = "XSAX0.S";
    // STORE_XA_PROTOCOL_VIOLATION_SQLSTATE has no associated message it is
    // just a constant used by the code so that an exception can be caught 
    // and programatically determined to be a STORE_XA_PROTOCOL_VIOLATION.
	String STORE_XA_PROTOCOL_VIOLATION_SQLSTATE                 = "XSAX0";
	String STORE_XA_XAER_DUPID                                  = "XSAX1.S";
    // STORE_XA_XAER_DUPID_SQLSTATE has no associated message it is
    // just a constant used by the code so that an exception can be caught 
    // and programatically determined to be a STORE_XA_XAER_DUPID.
	String STORE_XA_XAER_DUPID_SQLSTATE                         = "XSAX1";

	/*
	** Store - Conglomerate
	*/
    String CONGLOMERATE_TEMPLATE_CREATE_ERROR                   = "XSCG0.S";

	/*
	** Store - AccessManager
	*/
	String AM_NO_FACTORY_FOR_IMPLEMENTATION                     = "XSAM0.S";
	String AM_NO_SUCH_CONGLOMERATE_DROP                         = "XSAM2.S";
	String AM_NO_SUCH_CONGLOMERATE_TYPE                         = "XSAM3.S";
	String AM_NO_SUCH_SORT                                      = "XSAM4.S";
	String AM_SCAN_NOT_POSITIONED                               = "XSAM5.S";
	String AM_RECORD_NOT_FOUND                                  = "XSAM6.S";
	

	/*
	** Store - Heap
	*/
	String HEAP_CANT_CREATE_CONTAINER                           = "XSCH0.S";
	String HEAP_CONTAINER_NOT_FOUND                             = "XSCH1.S";
	String HEAP_COULD_NOT_CREATE_CONGLOMERATE                   = "XSCH4.S";
	String HEAP_TEMPLATE_MISMATCH                               = "XSCH5.S";
	String HEAP_IS_CLOSED                                       = "XSCH6.S";
	String HEAP_SCAN_NOT_POSITIONED                             = "XSCH7.S";
	String HEAP_UNIMPLEMENTED_FEATURE                           = "XSCH8.S";

	/*
	** Store - BTree
	*/
	String BTREE_CANT_CREATE_CONTAINER                          = "XSCB0.S";
	String BTREE_CONTAINER_NOT_FOUND                            = "XSCB1.S";
	String BTREE_PROPERTY_NOT_FOUND                             = "XSCB2.S";
	String BTREE_UNIMPLEMENTED_FEATURE                          = "XSCB3.S";
	String BTREE_SCAN_NOT_POSITIONED                            = "XSCB4.S";
	String BTREE_ROW_NOT_FOUND_DURING_UNDO                      = "XSCB5.S";
	String BTREE_NO_SPACE_FOR_KEY                               = "XSCB6.S";
	String BTREE_SCAN_INTERNAL_ERROR                            = "XSCB7.S";
	String BTREE_IS_CLOSED                                      = "XSCB8.S";
	String BTREE_ABORT_THROUGH_TRACE                            = "XSCB9.S";

	/*
	** Store - Sort
	*/
	String SORT_IMPROPER_SCAN_METHOD                            = "XSAS0.S";
	String SORT_SCAN_NOT_POSITIONED                             = "XSAS1.S";


	String SORT_TYPE_MISMATCH                                   = "XSAS3.S";
	String SORT_COULD_NOT_INIT                                  = "XSAS6.S";

	/*
	** RawStore
	*/

	/*
	** RawStore - protocol.Interface statement exceptions
	*/
    String RAWSTORE_NESTED_FREEZE                               = "XSRS0.S";
    String RAWSTORE_CANNOT_BACKUP_TO_NONDIRECTORY               = "XSRS1.S";
    String RAWSTORE_ERROR_RENAMING_FILE                         = "XSRS4.S";
    String RAWSTORE_ERROR_COPYING_FILE                          = "XSRS5.S";
    String RAWSTORE_CANNOT_CREATE_BACKUP_DIRECTORY              = "XSRS6.S";
    String RAWSTORE_UNEXPECTED_EXCEPTION                        = "XSRS7.S";
    String RAWSTORE_CANNOT_CHANGE_LOGDEVICE                     = "XSRS8.S";
    String RAWSTORE_RECORD_VANISHED                             = "XSRS9.S";
    String BACKUP_BLOCKING_OPERATIONS_IN_PROGRESS               = "XSRSA.S";
    String BACKUP_OPERATIONS_NOT_ALLOWED                        = "XSRSB.S";
    String RAWSTORE_CANNOT_BACKUP_INTO_DATABASE_DIRECTORY       = "XSRSC.S";

	/*
	** RawStore - Log.Generic statement exceptions
	*/
	String LOG_WRITE_LOG_RECORD                                 = "XSLB1.S";
	String LOG_BUFFER_FULL                                      = "XSLB2.S";
	String LOG_TRUNC_LWM_ILLEGAL                                = "XSLB5.S";
	String LOG_ZERO_LENGTH_LOG_RECORD                           = "XSLB6.S";
	String LOG_RESET_BEYOND_SCAN_LIMIT                          = "XSLB8.S";
	String LOG_FACTORY_STOPPED                                  = "XSLB9.S";

	/*
	** RawStore - Log.Generic database exceptions
	*/
	String LOG_CANNOT_FLUSH                                     = "XSLA0.D";
	String LOG_DO_ME_FAIL                                       = "XSLA1.D";
	String LOG_IO_ERROR                                         = "XSLA2.D";
	String LOG_CORRUPTED                                        = "XSLA3.D";
	String LOG_FULL                                             = "XSLA4.D";
	String LOG_READ_LOG_FOR_UNDO                                = "XSLA5.D";
	String LOG_RECOVERY_FAILED                                  = "XSLA6.D";
	String LOG_REDO_FAILED                                      = "XSLA7.D";
	String LOG_UNDO_FAILED                                      = "XSLA8.D";
	String LOG_STORE_CORRUPT                                    = "XSLAA.D";
	String LOG_FILE_NOT_FOUND                                   = "XSLAB.D";
	String LOG_INCOMPATIBLE_FORMAT                              = "XSLAC.D";
	String LOG_RECORD_CORRUPTED                                 = "XSLAD.D";
	String LOG_CONTROL_FILE                                     = "XSLAE.D";
	String LOG_READ_ONLY_DB_NEEDS_UNDO                          = "XSLAF.D";
	String LOG_READ_ONLY_DB_UPDATE                              = "XSLAH.D";
	String LOG_CANNOT_LOG_CHECKPOINT                            = "XSLAI.D";
	String LOG_NULL                                             = "XSLAJ.D";
	String LOG_EXCEED_MAX_LOG_FILE_NUMBER                       = "XSLAK.D";
	String LOG_EXCEED_MAX_LOG_FILE_SIZE                         = "XSLAL.D";
	String LOG_CANNOT_VERIFY_LOG_FORMAT                         = "XSLAM.D";
	String LOG_INCOMPATIBLE_VERSION                             = "XSLAN.D";
	String LOG_UNEXPECTED_RECOVERY_PROBLEM                      = "XSLAO.D";
	String LOG_CANNOT_UPGRADE_BETA                              = "XSLAP.D";
	String LOG_SEGMENT_NOT_EXIST                                = "XSLAQ.D";
	String UNABLE_TO_COPY_LOG_FILE                              = "XSLAR.D";
	String LOG_DIRECTORY_NOT_FOUND_IN_BACKUP                    = "XSLAS.D";
	String LOG_SEGMENT_EXIST                                    = "XSLAT.D";


	/*
	** RawStore - Transactions.Basic statement exceptions
	*/
	String XACT_MAX_SAVEPOINT_LEVEL_REACHED                     = "3B002.S";
	//Bug 4466 - changed sqlstate for following two to match DB2 sqlstates.
	String XACT_SAVEPOINT_EXISTS                                = "3B501.S";
	String XACT_SAVEPOINT_NOT_FOUND                             = "3B001.S";
	//Bug 4468 - release/rollback of savepoint failed because it doesn't exist 
	String XACT_SAVEPOINT_RELEASE_ROLLBACK_FAIL                 = "3B502.S";
	String XACT_TRANSACTION_ACTIVE                              = "XSTA2.S";

	/*
	** RawStore - Transactions.Basic transaction exceptions
	*/
	String TRANSACTION_PREFIX="40";

	String XACT_PROTOCOL_VIOLATION                              = "40XT0";
	String XACT_COMMIT_EXCEPTION                                = "40XT1";
	String XACT_ROLLBACK_EXCEPTION                              = "40XT2";
	String XACT_TRANSACTION_NOT_IDLE                            = "40XT4";
	String XACT_INTERNAL_TRANSACTION_EXCEPTION                  = "40XT5";
	String XACT_CANNOT_ACTIVATE_TRANSACTION                     = "40XT6";
	String XACT_NOT_SUPPORTED_IN_INTERNAL_XACT                  = "40XT7";
	String XACT_PROTOCOL_VIOLATION_DETAILED                     = "40XT8";

	/*
	** RawStore - Transactions.Basic system exceptions
	*/
	String XACT_ABORT_EXCEPTION                                 = "XSTB0.M";
	String XACT_CANNOT_LOG_CHANGE                               = "XSTB2.M";
	String XACT_CANNOT_ABORT_NULL_LOGGER                        = "XSTB3.M";
	String XACT_CREATE_NO_LOG                                   = "XSTB5.M";
	String XACT_TRANSACTION_TABLE_IN_USE                        = "XSTB6.M";

	
	/*
	** RawStore - Data.Generic statement exceptions
	*/
	String DATA_SLOT_NOT_ON_PAGE                                = "XSDA1.S";
	String DATA_UPDATE_DELETED_RECORD                           = "XSDA2.S";
	String DATA_NO_SPACE_FOR_RECORD                             = "XSDA3.S";
	String DATA_UNEXPECTED_EXCEPTION                            = "XSDA4.S";
	String DATA_UNDELETE_RECORD                                 = "XSDA5.S";
	String DATA_NULL_STORABLE_COLUMN                            = "XSDA6.S";
	String DATA_STORABLE_READ_MISMATCH                          = "XSDA7.S";
	String DATA_STORABLE_READ_EXCEPTION                         = "XSDA8.S";
	String DATA_STORABLE_READ_MISSING_CLASS                     = "XSDA9.S";
	String DATA_TIME_STAMP_ILLEGAL                              = "XSDAA.S";
	String DATA_TIME_STAMP_NULL                                 = "XSDAB.S";
	String DATA_DIFFERENT_CONTAINER                             = "XSDAC.S";
	String DATA_NO_ROW_COPIED                                   = "XSDAD.S";
	String DATA_CANNOT_MAKE_RECORD_HANDLE                       = "XSDAE.S";
	String DATA_INVALID_RECORD_HANDLE                           = "XSDAF.S";
	String DATA_ALLOC_NTT_CANT_OPEN                             = "XSDAG.S";
	String DATA_CANNOT_GET_DEALLOC_LOCK                         = "XSDAI.S";
	String DATA_STORABLE_WRITE_EXCEPTION                        = "XSDAJ.S";
	String DATA_WRONG_PAGE_FOR_HANDLE                           = "XSDAK.S";
	String DATA_UNEXPECTED_OVERFLOW_PAGE                        = "XSDAL.S";
    String DATA_SQLDATA_READ_INSTANTIATION_EXCEPTION            = "XSDAM.S";
    String DATA_SQLDATA_READ_ILLEGAL_ACCESS_EXCEPTION           = "XSDAN.S";
    String DATA_DOUBLE_LATCH_INTERNAL_ERROR                     = "XSDAO.S";
    String DATA_UNEXPECTED_NO_SPACE_ON_PAGE                     = "XSDAP.S";

	/*
	** RawStore - Data.Generic transaction exceptions
	*/
	String DATA_CORRUPT_PAGE                                    = "XSDB0.D";
	String DATA_UNKNOWN_PAGE_FORMAT                             = "XSDB1.D";
	String DATA_UNKNOWN_CONTAINER_FORMAT                        = "XSDB2.D";
	String DATA_CHANGING_CONTAINER_INFO                         = "XSDB3.D";
	String DATA_MISSING_LOG                                     = "XSDB4.D";
	String DATA_MISSING_PAGE                                    = "XSDB5.D";
	String DATA_MULTIPLE_JBMS_ON_DB                             = "XSDB6.D";
	String DATA_MULTIPLE_JBMS_WARNING                           = "XSDB7.D";
	String DATA_MULTIPLE_JBMS_FORCE_LOCK                        = "XSDB8.D";
	String DATA_CORRUPT_STREAM_CONTAINER                        = "XSDB9.D";
	String DATA_OBJECT_ALLOCATION_FAILED                        = "XSDBA.D";
	String DATA_UNKNOWN_PAGE_FORMAT_2                           = "XSDBB.D";
	String DATA_BAD_CONTAINERINFO_WRITE                         = "XSDBC.D";

	/*
	** RawStore - Data.Filesystem statement exceptions
	*/
	String FILE_EXISTS                                          = "XSDF0.S";
	String FILE_CREATE                                          = "XSDF1.S";
	String FILE_CREATE_NO_CLEANUP                               = "XSDF2.S";
	String FILE_CANNOT_CREATE_SEGMENT                           = "XSDF3.S";
	String FILE_CANNOT_REMOVE_FILE                              = "XSDF4.S";
	String FILE_NO_ALLOC_PAGE                                   = "XSDF6.S";
	String FILE_NEW_PAGE_NOT_LATCHED                            = "XSDF7.S";
	String FILE_REUSE_PAGE_NOT_FOUND                            = "XSDF8.S";
	String FILE_READ_ONLY                                       = "XSDFB.S";
	String FILE_IO_GARBLED                                      = "XSDFD.S";
	String FILE_UNEXPECTED_EXCEPTION                            = "XSDFF.S";
    String BACKUP_FILE_IO_ERROR                                 = "XSDFH.S";
	String FILE_NEW_PAGE_DURING_RECOVERY                        = "XSDFI.S";
	String FILE_CANNOT_REMOVE_ENCRYPT_FILE                      = "XSDFJ.S";
	String FILE_CANNOT_REMOVE_JAR_FILE                          = "XSDFK.S";

	/*
	** RawStore - Data.FSLDemo transaction exceptions
	*/

	/*
	** RawStore - Data.Filesystem database exceptions
	*/
	String FILE_READ_PAGE_EXCEPTION                             = "XSDG0.D";
	String FILE_WRITE_PAGE_EXCEPTION                            = "XSDG1.D";
	String FILE_BAD_CHECKSUM                                    = "XSDG2.D";
	String FILE_CONTAINER_EXCEPTION                             = "XSDG3.D";
    String UNABLE_TO_ARRAYCOPY                                  = "XSDG4.D";
	String FILE_DATABASE_NOT_IN_CREATE                          = "XSDG5.D";
	String DATA_DIRECTORY_NOT_FOUND_IN_BACKUP                   = "XSDG6.D";
	String UNABLE_TO_REMOVE_DATA_DIRECTORY                      = "XSDG7.D";
	String UNABLE_TO_COPY_DATA_DIRECTORY                        = "XSDG8.D";
	String FILE_IO_INTERRUPTED                                  = "XSDG9.D";



	/*
	** InternalUtil - Id Parsing 
	** Note that the code catches ID parsing errors.
	** (Range XCXA0-XCXAZ)
	*/
	String ID_PARSE_ERROR               ="XCXA0.S";

	/*
	** InternalUtil - Database Class Path Parsing
	** Note that the code catches database class path parsing errors.
	** (Range XCXB0-XCXBZ)
	*/
	String DB_CLASS_PATH_PARSE_ERROR="XCXB0.S";

	/*
	** InternalUtil - Id List Parsing
	** Note that the code catches id list parsing errors.
	** (Range XCXC0-XCXCZ)
	*/
	String ID_LIST_PARSE_ERROR="XCXC0.S";

	/*
	** InternalUtil - IO Errors
	** (Range XCXD0-XCXDZ)
	*/

	/*
	** InternalUtil - LocaleFinder interface
	*/
	String NO_LOCALE="XCXE0.S";

	String DATA_CONTAINER_CLOSED                = "40XD0";
	String DATA_CONTAINER_READ_ONLY             = "40XD1";
	String DATA_CONTAINER_VANISHED              = "40XD2";

	/*
	** Connectivity - Connection Exceptions: 08XXX
	*/
	String CONNECTIVITY_PREFIX="08";


    /*
	** Language
	*/

	/*
	** Language Statement Exception
	*/
	String LSE_COMPILATION_PREFIX="42";

	/*
	** Language
	**
	** The entries in this file are sorted into groups.  Add your entry
	** to the appropriate group. Language errors are divided into 3 groups:
	** A group for standard SQLExceptions.
	**
	** 2200J-00R - For SQL/XML errors (based on SQL/XML[2006]).
	** 4250x - access rule violations
	** 428?? - adding some DB2 compatible errors
	** 42X00-42Zxx for compilation errors 
	** 46000  for SQLJ errors (for now, leave this range empty)
	** 38000  SQL3 ranges 
	** 39001  SQL3
	** X0X00-X0Xxx for implementation-defined execution errors.
	**
	** NOTE: If an error can occur during both compilation and execution, then
	** you need 2 different errors.  
	**
	** In addition to the above groups, this file also contains SQLStates
	** for language transaction severity errors. These are in the range
	**
	**	40XC0 - 40XCZ
	**
	** implementation-defined range reserved for class 23 is L01-LZZ
	**
	**
	** Errors that have standard SQLStates
	**
	** Implementation-defined subclasses must begin with a digit from 5 through 9,
	** or a letter from I through Z (capitals only).
	**
 	*/

	/*
	**
	** SQL-J ERRORS -- see jamie for further info
	**
	** DDL
	**	46001 - invalid URL
	**	46002 - invalid JAR name
	**	46003 - invalid class deletion
	**	46004 - invalid JAR name
	** 	46005 - invalid replacement
	** 	46006 - invalid grantee
	** 	46007 - invalid signature
	** 	46008 - invalid method specification
	** 	46009 - invalid REVOKE
	**
	** Execution
	** 	46102 - invalid jar name in path
	** 	46103 - unresolved class name
	** 	0100E - too many result sets
	**	39001 - invalid SQLSTATE
	**	39004 - invalid null value
	**	38000 - uncaught java exception
	**	38mmm - user defined error numbers
	** to be used in the future
	** InvalidNullValue.sqlstate=39004
	*/

	// WARNINGS (start with 01)
	String LANG_CONSTRAINT_DROPPED									   = "01500";
	String LANG_VIEW_DROPPED										   = "01501";
	String LANG_TRIGGER_DROPPED										   = "01502";
	String LANG_COL_NOT_NULL									   	   = "01503";
	String LANG_INDEX_DUPLICATE									   	   = "01504";
	String LANG_VALUE_TRUNCATED                                        = "01505";
	String LANG_SYNONYM_UNDEFINED                                      = "01522";
	String LANG_NULL_ELIMINATED_IN_SET_FUNCTION						   = "01003";
	String LANG_PRIVILEGE_NOT_REVOKED						   		   = "01006";
	String LANG_ROLE_NOT_REVOKED                                       = "01007";
	String LANG_WITH_ADMIN_OPTION_NOT_REVOKED                          = "01008";
	String LANG_GEN_COL_DROPPED                                         = "01009";

	String LANG_NO_ROW_FOUND									   	   = "02000";

	String LANG_TOO_MANY_DYNAMIC_RESULTS_RETURNED					   = "0100E";

    // State used by java.sql.DataTruncation for truncation in read operations.
    String DATA_TRUNCATION_READ = "01004";

	// Invalid role specification: standard says class 0P, no subclass.
	String ROLE_INVALID_SPECIFICATION                                  = "0P000";
	String ROLE_INVALID_SPECIFICATION_NOT_GRANTED                      = "0P000.S.1";

	// TRANSACTION severity language errors. These are in the range:
	// 40XC0 - 40XCZ
	String LANG_DEAD_STATEMENT                                         = "40XC0";

	/*
	** SQL Data exceptions
	*/
	String SQL_DATA_PREFIX="22";
	
	String LANG_MISSING_PARMS                                          = "07000";
	String LANG_SCALAR_SUBQUERY_CARDINALITY_VIOLATION                  = "21000";
	String LANG_REDUNDANT_SUBJECT_ROW                         = "21000.S.1";
	String LANG_STRING_TRUNCATION                                      = "22001";
	String LANG_CONCAT_STRING_OVERFLOW                                      = "54006";
	String LANG_OUTSIDE_RANGE_FOR_DATATYPE                             = "22003";
    String YEAR_EXCEEDS_MAXIMUM                                        = "22003.S.1";
    String DECIMAL_TOO_MANY_DIGITS                                     = "22003.S.2";
    String NUMERIC_OVERFLOW                                            = "22003.S.3";
    String CLIENT_LENGTH_OUTSIDE_RANGE_FOR_DATATYPE                    = "22003.S.4";

	String LANG_DATA_TYPE_GET_MISMATCH                                 = "22005"; // same 22005 error
    String UNSUPPORTED_ENCODING                                        = "22005.S.1";
    String CHARACTER_CONVERTER_NOT_AVAILABLE                           = "22005.S.2";
    String CANT_CONVERT_UNICODE_TO_EBCDIC                              = "22005.S.3";
    String NET_UNRECOGNIZED_JDBC_TYPE                                  = "22005.S.4";
    String NET_INVALID_JDBC_TYPE_FOR_PARAM                             = "22005.S.5";
    String UNRECOGNIZED_JAVA_SQL_TYPE                                  = "22005.S.6";
    String CANT_CONVERT_UNICODE_TO_UTF8                                = "22005.S.7";

	String LANG_DATE_RANGE_EXCEPTION                                   = "22007.S.180";
	String LANG_DATE_SYNTAX_EXCEPTION                                  = "22007.S.181";
    String LANG_INVALID_FUNCTION_ARGUMENT                              = "22008.S";
    String LANG_SEQUENCE_GENERATOR_EXHAUSTED                              = "2200H.S";
	String LANG_SUBSTR_START_OR_LEN_OUT_OF_RANGE                        = "22011";
	String LANG_SUBSTR_START_ADDING_LEN_OUT_OF_RANGE                        = "22011.S.1";
	String LANG_DIVIDE_BY_ZERO                                         = "22012";
    String LANG_SQRT_OF_NEG_NUMBER                                     = "22013";
    String LANG_INVALID_PARAMETER_FOR_SEARCH_POSITION                  = "22014";
    String LANG_INVALID_TYPE_FOR_LOCATE_FUNCTION                       = "22015";
	String LANG_FORMAT_EXCEPTION                                       = "22018";
	String LANG_INVALID_ESCAPE_CHARACTER                               = "22019";
	String LANG_INVALID_TRIM_CHARACTER                                 = "22020";
	String LANG_INVALID_ESCAPE_SEQUENCE                                = "22025";
	String LANG_INVALID_TRIM_SET                                       = "22027";
    String LANG_STRING_TOO_LONG                                        = "22028";
	String LANG_ESCAPE_IS_NULL                                  	   = "22501";
	String LANG_INVALID_ROW_COUNT_FIRST                                = "2201W";
	String LANG_INVALID_ROW_COUNT_OFFSET                               = "2201X";
	String LANG_ROW_COUNT_OFFSET_FIRST_IS_NULL                         = "2201Z";

	/*
	** Integrity violations.
	*/
	String INTEGRITY_VIOLATION_PREFIX="23";
	
	String LANG_NULL_INTO_NON_NULL                                     = "23502";
	String LANG_DUPLICATE_KEY_CONSTRAINT                               = "23505";
    String LANG_DEFERRED_DUPLICATE_KEY_CONSTRAINT_T                    = "23506.T.1";
    String LANG_DEFERRED_DUPLICATE_KEY_CONSTRAINT_S                    = "23507.S.1";
	String LANG_FK_VIOLATION                                           = "23503";
	String LANG_CHECK_CONSTRAINT_VIOLATED                              = "23513";
    String LANG_DEFERRED_CHECK_CONSTRAINT_T                            = "23514.T.1";
    String LANG_DEFERRED_CHECK_CONSTRAINT_S                            = "23515.S.1";

    String LANG_DEFERRED_FK_CONSTRAINT_T                               = "23516.T.1";
    String LANG_DEFERRED_FK_CONSTRAINT_S                               = "23517.S.1";
	// From SQL/XML[2006] spec; there are others, but
	// these are the ones we actually use with our
	// current XML support.
	String LANG_XML_QUERY_ERROR                                        = "10000";
	String LANG_NOT_AN_XML_DOCUMENT                                    = "2200L";
	String LANG_INVALID_XML_DOCUMENT                                   = "2200M";
	String LANG_INVALID_XML_CONTEXT_ITEM                               = "2200V";
	String LANG_XQUERY_SERIALIZATION_ERROR                             = "2200W";

    String CANNOT_CLOSE_ACTIVE_CONNECTION                              = "25001";
    String INVALID_TRANSACTION_STATE_ACTIVE_CONNECTION                 = "25001.S.1";


	String LANG_UNEXPECTED_USER_EXCEPTION                              = "38000";
	String EXTERNAL_ROUTINE_NO_SQL									   = "38001";
	String EXTERNAL_ROUTINE_NO_MODIFIES_SQL							   = "38002";
	String EXTERNAL_ROUTINE_NO_READS_SQL							   = "38004";

	String LANG_NULL_TO_PRIMITIVE_PARAMETER                            = "39004";
	String LANG_SYNTAX_OR_ACCESS_VIOLATION                             = "42000";

	String AUTH_NO_TABLE_PERMISSION                                    = "42500";
	String AUTH_NO_TABLE_PERMISSION_FOR_GRANT                          = "42501";
	String AUTH_NO_COLUMN_PERMISSION                                   = "42502";
	String AUTH_NO_COLUMN_PERMISSION_FOR_GRANT                         = "42503";
	String AUTH_NO_GENERIC_PERMISSION                                  = "42504";
	String AUTH_NO_GENERIC_PERMISSION_FOR_GRANT                        = "42505";
	String AUTH_NOT_OWNER                                              = "42506";
	String AUTH_NO_ACCESS_NOT_OWNER                                    = "42507";
	String AUTH_NOT_DATABASE_OWNER                                     = "42508";
	String AUTH_GRANT_REVOKE_NOT_ALLOWED                               = "42509";
	String AUTH_NO_OBJECT_PERMISSION                                   = "4250A";
	String AUTH_INVALID_AUTHORIZATION_PROPERTY                         = "4250B";
	String AUTH_USER_IN_READ_AND_WRITE_LISTS                           = "4250C";
	String AUTH_DUPLICATE_USERS                                        = "4250D";
	String AUTH_INTERNAL_BAD_UUID                                      = "4250E";
    String AUTH_ROLE_DBO_ONLY                                          = "4251A";
	String AUTH_PUBLIC_ILLEGAL_AUTHORIZATION_ID                        = "4251B";
	String AUTH_ROLE_GRANT_CIRCULARITY                                 = "4251C";
	String DBO_ONLY                                                         = "4251D";
	String HIDDEN_COLUMN                                                         = "4251E";
	String CANT_DROP_DBO                                                         = "4251F";
	String WEAK_AUTHENTICATION                                               = "4251G";
	String BAD_NATIVE_AUTH_SPEC                                               = "4251H";
	String MISSING_CREDENTIALS_DB                                               = "4251I";
	String BAD_PASSWORD_LIFETIME                                               = "4251J";
	String DBO_FIRST                                                                    = "4251K";
	String BAD_CREDENTIALS_DB_NAME                                          = "4251L";

	String LANG_DB2_NOT_NULL_COLUMN_INVALID_DEFAULT                    = "42601";
	String LANG_DB2_INVALID_HEXADECIMAL_CONSTANT                    = "42606";
	String LANG_DB2_STRING_CONSTANT_TOO_LONG                    = "54002";
	String LANG_DB2_NUMBER_OF_ARGS_INVALID                   = "42605";
	String LANG_DB2_COALESCE_FUNCTION_ALL_PARAMS                   = "42610";
	String LANG_DB2_LENGTH_PRECISION_SCALE_VIOLATION                   = "42611";
	String LANG_DB2_MULTIPLE_ELEMENTS								   = "42613";
	String LANG_DB2_INVALID_CHECK_CONSTRAINT                           = "42621";
	String LANG_DB2_DUPLICATE_NAMES									   = "42734";
	String LANG_DB2_INVALID_COLS_SPECIFIED                             = "42802";
        String LANG_DB2_INVALID_SELECT_COL_FOR_HAVING = "42803";
	String LANG_DB2_ADD_UNIQUE_OR_PRIMARY_KEY_ON_NULL_COLS			   = "42831";
	String LANG_ADD_PRIMARY_KEY_ON_NULL_COLS                           = "42831.S.1";
	String LANG_DB2_REPLACEMENT_ERROR								   = "42815.S.713";
	String LANG_DB2_COALESCE_DATATYPE_MISMATCH								   = "42815.S.171";
	String LANG_DB2_TOO_LONG_FLOATING_POINT_LITERAL			           = "42820";
	String LANG_DB2_LIKE_SYNTAX_ERROR 						           = "42824";
	String LANG_INVALID_FK_COL_FOR_SETNULL                             = "42834";
	String LANG_INVALID_ALTER_TABLE_ATTRIBUTES                         = "42837";
	String LANG_DB2_FUNCTION_INCOMPATIBLE                              = "42884";


	String LANG_DB2_PARAMETER_NEEDS_MARKER							   = "42886";
    String LANG_DB2_INVALID_DEFAULT_VALUE                              = "42894";

	String LANG_NO_AGGREGATES_IN_WHERE_CLAUSE                          = "42903";
	String LANG_DB2_VIEW_REQUIRES_COLUMN_NAMES                         = "42908";
	String LANG_TABLE_REQUIRES_COLUMN_NAMES                            = "42909";
	String LANG_DELETE_RULE_VIOLATION		   					       = "42915";
	String LANG_SYNONYM_CIRCULAR   		   					           = "42916";
	String LANG_SYNTAX_ERROR                                           = "42X01";
	String LANG_LEXICAL_ERROR                                          = "42X02";
	String LANG_AMBIGUOUS_COLUMN_NAME                                  = "42X03";
	String LANG_COLUMN_NOT_FOUND                                       = "42X04";
	String LANG_TABLE_NOT_FOUND                                        = "42X05";
	String LANG_TOO_MANY_RESULT_COLUMNS                                = "42X06";
	String LANG_NULL_IN_VALUES_CLAUSE                                  = "42X07";
	String LANG_DOES_NOT_IMPLEMENT			                          = "42X08";
	String LANG_FROM_LIST_DUPLICATE_TABLE_NAME                         = "42X09";
	String LANG_EXPOSED_NAME_NOT_FOUND                                 = "42X10";
	String LANG_IDENTIFIER_TOO_LONG                                    = "42622";
	String LANG_DUPLICATE_COLUMN_NAME_CREATE                           = "42X12";
	String LANG_TOO_MANY_COLUMNS_IN_TABLE_OR_VIEW                         = "54011";
	String LANG_TOO_MANY_INDEXES_ON_TABLE                         = "42Z9F";
	String LANG_DUPLICATE_COLUMN_NAME_INSERT                           = "42X13";
	String LANG_COLUMN_NOT_FOUND_IN_TABLE                              = "42X14";
	String LANG_ILLEGAL_COLUMN_REFERENCE                               = "42X15";
	String LANG_DUPLICATE_COLUMN_NAME_UPDATE                           = "42X16";
	String LANG_INVALID_JOIN_ORDER_SPEC                                = "42X17";
	String LANG_NOT_COMPARABLE                                         = "42818";
	String LANG_NON_BOOLEAN_WHERE_CLAUSE                               = "42X19.S.1";
	String LANG_UNTYPED_PARAMETER_IN_WHERE_CLAUSE        = "42X19.S.2";
	String LANG_INTEGER_LITERAL_EXPECTED                               = "42X20";
	String LANG_CURSOR_NOT_UPDATABLE                                   = "42X23";
	String LANG_INVALID_COL_HAVING_CLAUSE                              = "42X24";
	String LANG_UNARY_FUNCTION_BAD_TYPE                                = "42X25";
	String LANG_TYPE_DOESNT_EXIST                                      = "42X26";
	String LANG_CURSOR_DELETE_MISMATCH                                 = "42X28";
	String LANG_CURSOR_UPDATE_MISMATCH                                 = "42X29";
	String LANG_CURSOR_NOT_FOUND                                       = "42X30";
	String LANG_COLUMN_NOT_UPDATABLE_IN_CURSOR                         = "42X31";
	String LANG_CORRELATION_NAME_FOR_UPDATABLE_COLUMN_DISALLOWED_IN_CURSOR = "42X42";
	String LANG_DERIVED_COLUMN_LIST_MISMATCH                           = "42X32";
	String LANG_DUPLICATE_COLUMN_NAME_DERIVED                          = "42X33";
	String LANG_PARAM_IN_SELECT_LIST                                   = "42X34";
	String LANG_BINARY_OPERANDS_BOTH_PARMS                             = "42X35";
	String LANG_UNARY_OPERAND_PARM                                     = "42X36";
	String LANG_UNARY_ARITHMETIC_BAD_TYPE                              = "42X37";
	String LANG_CANT_SELECT_STAR_SUBQUERY                              = "42X38";
	String LANG_NON_SINGLE_COLUMN_SUBQUERY                             = "42X39";
	String LANG_UNARY_LOGICAL_NON_BOOLEAN                              = "42X40";
	String LANG_INVALID_FROM_LIST_PROPERTY                             = "42X41";
	String LANG_NOT_STORABLE                                           = "42821";
	String LANG_NULL_RESULT_SET_META_DATA                              = "42X43";
	String LANG_INVALID_COLUMN_LENGTH                                  = "42X44";
	String LANG_INVALID_FUNCTION_ARG_TYPE                              = "42X45";
	String LANG_AMBIGUOUS_FUNCTION_NAME                                = "42X46";
	String LANG_AMBIGUOUS_PROCEDURE_NAME                               = "42X47";
	String LANG_INVALID_PRECISION                                      = "42X48";
	String LANG_INVALID_INTEGER_LITERAL                                = "42X49";
	String LANG_NO_METHOD_FOUND                                        = "42X50";
	String LANG_TYPE_DOESNT_EXIST2                                     = "42X51";
	String LANG_PRIMITIVE_RECEIVER                                     = "42X52";
	String LANG_LIKE_BAD_TYPE                                          = "42X53";
	String LANG_PARAMETER_RECEIVER                                     = "42X54";
	String LANG_TABLE_NAME_MISMATCH                                    = "42X55";
	String LANG_VIEW_DEFINITION_R_C_L_MISMATCH                         = "42X56";
	String LANG_INVALID_V_T_I_COLUMN_COUNT                             = "42X57";
	String LANG_UNION_UNMATCHED_COLUMNS                                = "42X58";
	String LANG_ROW_VALUE_CONSTRUCTOR_UNMATCHED_COLUMNS                = "42X59";
	String LANG_INVALID_INSERT_MODE                                    = "42X60";
	String LANG_NOT_UNION_COMPATIBLE                                   = "42X61";
	String LANG_NO_USER_DDL_IN_SYSTEM_SCHEMA                           = "42X62";
	String LANG_NO_ROWS_FROM_USING                                     = "42X63";
	String LANG_INVALID_STATISTICS_SPEC								   = "42X64";
	String LANG_INDEX_NOT_FOUND                                        = "42X65";
	String LANG_DUPLICATE_COLUMN_NAME_CREATE_INDEX                     = "42X66";
	//42X67
	String LANG_NO_FIELD_FOUND                                         = "42X68";
	String LANG_PRIMITIVE_REFERENCING_EXPRESSION                       = "42X69";
	String LANG_TABLE_DEFINITION_R_C_L_MISMATCH                        = "42X70";
	String LANG_INVALID_COLUMN_TYPE_CREATE_TABLE                       = "42X71";
	String LANG_NO_STATIC_FIELD_FOUND                                  = "42X72";
	String LANG_AMBIGUOUS_METHOD_INVOCATION                            = "42X73";
	String LANG_INVALID_CALL_STATEMENT                                 = "42X74";
	String LANG_NO_CONSTRUCTOR_FOUND                                   = "42X75";
	String LANG_ADDING_PRIMARY_KEY_ON_EXPLICIT_NULLABLE_COLUMN         = "42X76";
	String LANG_COLUMN_OUT_OF_RANGE                                    = "42X77";
	String LANG_ORDER_BY_COLUMN_NOT_FOUND                              = "42X78";
	String LANG_DUPLICATE_COLUMN_FOR_ORDER_BY                          = "42X79";
	String LANG_QUALIFIED_COLUMN_NAME_NOT_ALLOWED                      = "42877";
    String LANG_UNION_ORDER_BY                                         = "42878";
	String LANG_DISTINCT_ORDER_BY                                      = "42879";
	String LANG_DISTINCT_ORDER_BY_EXPRESSION                           = "4287A";
    String LANG_TABLE_VALUE_CTOR_RESTRICTION                           = "4287B";
	String LANG_EMPTY_VALUES_CLAUSE                                    = "42X80";
	String LANG_EMPTY_COLUMN_LIST                                      = "42X81";
	String LANG_USING_CARDINALITY_VIOLATION                            = "42X82";
	String LANG_CANT_DROP_BACKING_INDEX                                = "42X84";
	String LANG_CONSTRAINT_SCHEMA_MISMATCH                             = "42X85";
    String LANG_DROP_OR_ALTER_NON_EXISTING_CONSTRAINT                  = "42X86";
    String LANG_ALL_RESULT_EXPRESSIONS_UNTYPED                         = "42X87";
	String LANG_CONDITIONAL_NON_BOOLEAN                                = "42X88";
	String LANG_NOT_TYPE_COMPATIBLE                                    = "42X89";
	String LANG_TOO_MANY_PRIMARY_KEY_CONSTRAINTS                       = "42X90";
	String LANG_DUPLICATE_CONSTRAINT_NAME_CREATE                       = "42X91";
	String LANG_DUPLICATE_CONSTRAINT_COLUMN_NAME                       = "42X92";
	String LANG_INVALID_CREATE_CONSTRAINT_COLUMN_LIST                  = "42X93";
	String LANG_OBJECT_NOT_FOUND                                       = "42X94";
	String LANG_DB_CLASS_PATH_HAS_MISSING_JAR                          = "42X96";
    String LANG_INCONSISTENT_CONSTRAINT_CHARACTERISTICS                = "42X97";
	String LANG_NO_PARAMS_IN_VIEWS                                     = "42X98";
	String LANG_NO_PARAMS_IN_TABLES                                    = "42X99";
    String LANG_UNASSIGNABLE_GENERATION_CLAUSE                         = "42XA0";
    String LANG_AGGREGATE_IN_GENERATION_CLAUSE                         = "42XA1";
    String LANG_NON_DETERMINISTIC_GENERATION_CLAUSE                    = "42XA2";
    String LANG_CANT_OVERRIDE_GENERATION_CLAUSE                        = "42XA3";
    String LANG_CANT_REFERENCE_GENERATED_COLUMN                        = "42XA4";
    String LANG_ROUTINE_CANT_PERMIT_SQL                                = "42XA5";
    String LANG_BAD_FK_ON_GENERATED_COLUMN                             = "42XA6";
    String LANG_GEN_COL_DEFAULT                                        = "42XA7";
    String LANG_GEN_COL_BAD_RENAME                                     = "42XA8";
    String LANG_NEEDS_DATATYPE                                         = "42XA9";
    String LANG_GEN_COL_BEFORE_TRIG                                    = "42XAA";
    String LANG_NOT_NULL_NEEDS_DATATYPE                                = "42XAB";
    String LANG_SEQ_INCREMENT_ZERO                                     = "42XAC";
    String LANG_SEQ_ARG_OUT_OF_DATATYPE_RANGE                          = "42XAE";
    String LANG_SEQ_MIN_EXCEEDS_MAX                                    = "42XAF";
    String LANG_SEQ_INVALID_START                                      = "42XAG";    
    String LANG_NEXT_VALUE_FOR_ILLEGAL                                      = "42XAH";    
    String LANG_SEQUENCE_REFERENCED_TWICE                                      = "42XAI";    
    String LANG_DUPLICATE_CS_CLAUSE                                      = "42XAJ";    
    String LANG_TARGET_NOT_BASE_TABLE                                  = "42XAK";    
    String LANG_SOURCE_NOT_BASE_OR_VTI                        = "42XAL";    
    String LANG_SAME_EXPOSED_NAME                                       = "42XAM";    
    String LANG_NOT_NULL_CHARACTERISTICS                               = "42XAN";
    String LANG_NO_SUBQUERIES_IN_MATCHED_CLAUSE         = "42XAO";
    String LANG_NO_SYNONYMS_IN_MERGE                            = "42XAP";
    String LANG_NO_DCL_IN_MERGE                                         = "42XAQ";
    String LANG_SYSTEM_SEQUENCE                                         = "42XAR";
    String LANG_BAD_DISTINCT_AGG                                         = "42XAS";
    String LANG_INVALID_ROWID_SCOPE                                      = "42XAT";
    String LANG_INVALID_INPUT_COLUMN_NAME                                = "42XAU";
    String LANG_INVALID_NUMBEROF_HEADER_LINES                            = "42XAV";
    String LANG_INVALID_USER_AGGREGATE_DEFINITION2                     = "42Y00";
	String LANG_INVALID_CHECK_CONSTRAINT                               = "42Y01";
    // String LANG_NO_ALTER_TABLE_COMPRESS_ON_TARGET_TABLE             = "42Y02";
	String LANG_NO_SUCH_METHOD_ALIAS                                   = "42Y03.S.0";
	String LANG_NO_SUCH_PROCEDURE                                      = "42Y03.S.1";
	String LANG_NO_SUCH_FUNCTION                                       = "42Y03.S.2";
    String LANG_PROC_USED_AS_FUNCTION                                  = "42Y03.S.3";
    String LANG_FUNCTION_USED_AS_PROC                                  = "42Y03.S.4";
	String LANG_INVALID_FULL_STATIC_METHOD_NAME                        = "42Y04";
	String LANG_NO_SUCH_FOREIGN_KEY                                    = "42Y05";
	//String LANG_METHOD_ALIAS_NOT_FOUND                                 = "42Y06";
	String LANG_SCHEMA_DOES_NOT_EXIST                                  = "42Y07";
	String LANG_NO_FK_ON_SYSTEM_SCHEMA                                 = "42Y08";
	String LANG_VOID_METHOD_CALL                                       = "42Y09";
	String LANG_TABLE_CONSTRUCTOR_ALL_PARAM_COLUMN                     = "42Y10";
	String LANG_MISSING_JOIN_SPECIFICATION                             = "42Y11";
	String LANG_NON_BOOLEAN_JOIN_CLAUSE                                = "42Y12";
	String LANG_DUPLICATE_COLUMN_NAME_CREATE_VIEW                      = "42Y13";
	// String LANG_DROP_TABLE_ON_NON_TABLE                                = "42Y15"; -- replaced by 42Y62
	String LANG_NO_METHOD_MATCHING_ALIAS                               = "42Y16";
	// String LANG_DROP_SYSTEM_TABLE_ATTEMPTED                         = "42Y17"; -- replaced by 42X62
	String LANG_INVALID_CAST                                           = "42846";
    //	String LANG_AMBIGUOUS_GROUPING_COLUMN                              = "42Y19"; -- unused post 883.
	//	String LANG_UNMATCHED_GROUPING_COLUMN                              =	//	"42Y20"; -- not used
	String LANG_USER_AGGREGATE_BAD_TYPE                                = "42Y22";
	String LANG_BAD_J_D_B_C_TYPE_INFO                                  = "42Y23";
	String LANG_VIEW_NOT_UPDATEABLE                                    = "42Y24";
	String LANG_UPDATE_SYSTEM_TABLE_ATTEMPTED                          = "42Y25";
    String LANG_AGGREGATE_IN_GROUPBY_LIST                              = "42Y26.S.0";
    String LANG_SUBQUERY_IN_GROUPBY_LIST                               = "42Y26.S.1";
	String LANG_NO_PARAMS_IN_TRIGGER_ACTION                            = "42Y27";
	// String LANG_NO_TRIGGER_ON_SYSTEM_TABLE                             = "42Y28"; -- replaced by 42X62
	String LANG_INVALID_NON_GROUPED_SELECT_LIST                        = "42Y29";
	String LANG_INVALID_GROUPED_SELECT_LIST                            = "42Y30";
	
	String LANG_TOO_MANY_ELEMENTS                            = "54004";
	String LANG_BAD_AGGREGATOR_CLASS2                                  = "42Y32";
	String LANG_USER_AGGREGATE_CONTAINS_AGGREGATE                      = "42Y33";
	String LANG_AMBIGUOUS_COLUMN_NAME_IN_TABLE                         = "42Y34";
	String LANG_INVALID_COL_REF_NON_GROUPED_SELECT_LIST                = "42Y35";
	String LANG_INVALID_COL_REF_GROUPED_SELECT_LIST                    = "42Y36";
	String LANG_TYPE_DOESNT_EXIST3                                     = "42Y37";
	String LANG_INVALID_BULK_INSERT_REPLACE                            = "42Y38";
    String LANG_UNRELIABLE_CHECK_CONSTRAINT                            = "42Y39";
	String LANG_DUPLICATE_COLUMN_IN_TRIGGER_UPDATE                     = "42Y40";
	String LANG_TRIGGER_SPS_CANNOT_BE_EXECED                           = "42Y41";
	String LANG_INVALID_DECIMAL_SCALE                                  = "42Y42";
	String LANG_INVALID_DECIMAL_PRECISION_SCALE                        = "42Y43";
	String LANG_INVALID_FROM_TABLE_PROPERTY                            = "42Y44";
	String LANG_CANNOT_BIND_TRIGGER_V_T_I                              = "42Y45";
	String LANG_INVALID_FORCED_INDEX1                                  = "42Y46";
//	String LANG_INVALID_FORCED_INDEX2                                  = "42Y47";
	String LANG_INVALID_FORCED_INDEX2                                  = "42Y48";
	String LANG_DUPLICATE_PROPERTY                                     = "42Y49";
	String LANG_BOTH_FORCE_INDEX_AND_CONSTRAINT_SPECIFIED              = "42Y50";
//	String LANG_INVALID_FORCED_INDEX4                                  = "42Y51";
	String LANG_OBJECT_DOES_NOT_EXIST                                  = "42Y55";
	String LANG_INVALID_JOIN_STRATEGY                                  = "42Y56";
	String LANG_INVALID_NUMBER_FORMAT_FOR_OVERRIDE                     = "42Y58";
	String LANG_INVALID_HASH_INITIAL_CAPACITY                          = "42Y59";
	String LANG_INVALID_HASH_LOAD_FACTOR                               = "42Y60";
	String LANG_INVALID_HASH_MAX_CAPACITY                              = "42Y61";
	String LANG_INVALID_OPERATION_ON_VIEW                              = "42Y62";
	String LANG_HASH_NO_EQUIJOIN_FOUND                                 = "42Y63";
	String LANG_INVALID_BULK_FETCH_VALUE                               = "42Y64";
	String LANG_INVALID_BULK_FETCH_WITH_JOIN_TYPE                      = "42Y65";
	String LANG_INVALID_BULK_FETCH_UPDATEABLE                          = "42Y66";
	String LANG_CANNOT_DROP_SYSTEM_SCHEMAS                             = "42Y67";
	String LANG_NO_BEST_PLAN_FOUND                                     = "42Y69";
	String LANG_ILLEGAL_FORCED_JOIN_ORDER                              = "42Y70";
	String LANG_CANNOT_DROP_SYSTEM_ALIASES                             = "42Y71";
	String LANG_CANNOT_DROP_TRIGGER_S_P_S                              = "42Y82";
	String LANG_USER_AGGREGATE_BAD_TYPE_NULL                           = "42Y83";
	String LANG_INVALID_DEFAULT_DEFINITION                             = "42Y84";
	String LANG_INVALID_USE_OF_DEFAULT                                 = "42Y85";
	String LANG_STMT_NOT_UPDATABLE                                     = "42Y90";
	String LANG_NO_SPS_USING_IN_TRIGGER                                = "42Y91";
	String LANG_TRIGGER_BAD_REF_MISMATCH                               = "42Y92";
	String LANG_TRIGGER_BAD_REF_CLAUSE_DUPS                            = "42Y93";
	String LANG_BINARY_LOGICAL_NON_BOOLEAN                             = "42Y94";
	String LANG_BINARY_OPERATOR_NOT_SUPPORTED                          = "42Y95";
	String LANG_INVALID_ESCAPE										   = "42Y97";
    String LANG_UNRELIABLE_QUERY_FRAGMENT                              = "42Y98";
	String LANG_JAVA_METHOD_CALL_OR_FIELD_REF						   = "42Z00.U";
	String LANG_UNTYPED												   = "42Z01.U";
	// TEMPORARY COMPILATION RESTRICTIONS
	String LANG_USER_AGGREGATE_MULTIPLE_DISTINCTS                      = "42Z02";
	String LANG_NO_AGGREGATES_IN_ON_CLAUSE                             = "42Z07";
	String LANG_NO_BULK_INSERT_REPLACE_WITH_TRIGGER                    = "42Z08";
    String LANG_NO_AGGREGATES_IN_MERGE_MATCHING_CLAUSE                 = "42Z09";

	// MORE GENERIC LANGUAGE STUFF
	String LANG_UDT_BUILTIN_CONFLICT										   = "42Z10";
    String LANG_STREAM_INVALID_ACCESS                                  = "42Z12.U";

	// String LANG_UPDATABLE_VTI_BAD_GETMETADATA						   = "42Z14";

	// for alter table modify column ...
	String LANG_MODIFY_COLUMN_CHANGE_TYPE							   = "42Z15";
	String LANG_MODIFY_COLUMN_INVALID_TYPE							   = "42Z16";
	String LANG_MODIFY_COLUMN_INVALID_LENGTH						   = "42Z17";
	String LANG_MODIFY_COLUMN_FKEY_CONSTRAINT						   = "42Z18";
	String LANG_MODIFY_COLUMN_REFERENCED							   = "42Z19";
	String LANG_MODIFY_COLUMN_EXISTING_CONSTRAINT					   = "42Z20";
	String LANG_MODIFY_COLUMN_EXISTING_PRIMARY_KEY					   = "42Z20.S.1";

	String LANG_AI_INVALID_INCREMENT								   = "42Z21";
	String LANG_AI_INVALID_TYPE										   = "42Z22";
	String LANG_AI_CANNOT_MODIFY_AI									   = "42Z23";
	String LANG_AI_OVERFLOW											   = "42Z24";
	String LANG_AI_COUNTER_ERROR									   = "42Z25";
	String LANG_AI_CANNOT_NULL_AI									   = "42Z26";
	String LANG_AI_CANNOT_ADD_AI_TO_NULLABLE						   = "42Z27";
	// String LANG_BUILT_IN_ALIAS_NAME						   = "42Z28";
	// RUNTIMESTATISTICS
	String LANG_AI_CANNOT_ALTER_IDENTITYNESS						   = "42Z29";
	String LANG_TIME_SPENT_THIS										   = "42Z30.U";
	String LANG_TIME_SPENT_THIS_AND_BELOW							   = "42Z31.U";
	String LANG_TOTAL_TIME_BREAKDOWN								   = "42Z32.U";
	String LANG_CONSTRUCTOR_TIME									   = "42Z33.U";
	String LANG_OPEN_TIME											   = "42Z34.U";
	String LANG_NEXT_TIME											   = "42Z35.U";
	String LANG_CLOSE_TIME											   = "42Z36.U";
	String LANG_NONE												   = "42Z37.U";
	String LANG_POSITION_NOT_AVAIL									   = "42Z38.U";
	String LANG_UNEXPECTED_EXC_GETTING_POSITIONER					   = "42Z39.U";
	String LANG_POSITIONER											   = "42Z40.U";
	String LANG_ORDERED_NULL_SEMANTICS								   = "42Z41.U";
	String LANG_COLUMN_ID											   = "42Z42.U";
	String LANG_OPERATOR											   = "42Z43.U";
	String LANG_ORDERED_NULLS										   = "42Z44.U";
	String LANG_UNKNOWN_RETURN_VALUE								   = "42Z45.U";
	String LANG_NEGATE_COMPARISON_RESULT							   = "42Z46.U";
	String LANG_GQPT_NOT_SUPPORTED									   = "42Z47.U";
	String LANG_COLUMN_ID_ARRAY										   = "42Z48.U";

	String LANG_GRANT_REVOKE_WITH_LEGACY_ACCESS                        = "42Z60";

	// For Derby-specific XML compilation errors (not defined by SQL/XML standard).
	String LANG_ATTEMPT_TO_BIND_XML                                    = "42Z70";
	String LANG_ATTEMPT_TO_SELECT_XML                                  = "42Z71";
	String LANG_XML_KEYWORD_MISSING                                    = "42Z72";
	String LANG_INVALID_XMLSERIALIZE_TYPE                              = "42Z73";
	String LANG_UNSUPPORTED_XML_FEATURE                                = "42Z74";
	String LANG_INVALID_XML_QUERY_EXPRESSION                           = "42Z75";
	String LANG_MULTIPLE_XML_CONTEXT_ITEMS                             = "42Z76";
	String LANG_INVALID_CONTEXT_ITEM_TYPE                              = "42Z77";
	String LANG_XMLPARSE_UNKNOWN_PARAM_TYPE                            = "42Z79";

	String LANG_SERIALIZABLE										   = "42Z80.U";
	String LANG_READ_COMMITTED										   = "42Z81.U";
	String LANG_EXCLUSIVE											   = "42Z82.U";
	String LANG_INSTANTANEOUS_SHARE									   = "42Z83.U";
	String LANG_SHARE												   = "42Z84.U";
	String LANG_TABLE												   = "42Z85.U";
	String LANG_ROW													   = "42Z86.U";
	String LANG_SHARE_TABLE											   = "42Z87.U";
	String LANG_SHARE_ROW											   = "42Z88.U";

    // MORE GENERIC LANGUAGE STUFF
    // String LANG_UPDATABLE_VTI_BAD_GETRESULTSETCONCURRENCY          = "42Z89";
    String LANG_UPDATABLE_VTI_NON_UPDATABLE_RS                        = "42Z90";
    String LANG_SUBQUERY                                              = "42Z91";
    String LANG_REPEATABLE_READ                                       = "42Z92";
    String LANG_MULTIPLE_CONSTRAINTS_WITH_SAME_COLUMNS                = "42Z93";
    // String LANG_ALTER_SYSTEM_TABLE_ATTEMPTED                       = "42Z94"; -- replaced by 42X62
    // String LANG_ALTER_TABLE_ON_NON_TABLE                           = "42Z95"; -- replaced by 42Y62
    String LANG_RENAME_COLUMN_WILL_BREAK_CHECK_CONSTRAINT             = "42Z97";
    // beetle 2758.  For now just raise an error for literals > 64K
    String LANG_INVALID_LITERAL_LENGTH                                = "42Z99";
    String LANG_READ_UNCOMMITTED                                      = "42Z9A";
    String LANG_UNSUPPORTED_TRIGGER_STMT                              = "42Z9D";
    String LANG_UNSUPPORTED_TRIGGER_PROC                              = "42Z9D.S.1";
    String LANG_DROP_CONSTRAINT_TYPE                                  = "42Z9E";
    String LANG_QUERY_TOO_COMPLEX                                     = "42ZA0";
    String LANG_INVALID_SQL_IN_BATCH                                  = "42ZA1";
    String LANG_LIKE_COLLATION_MISMATCH                               = "42ZA2";
    String LANG_CAN_NOT_CREATE_TABLE                               = "42ZA3";

    String LANG_NO_DJRS                                             = "42ZB1";
    String LANG_MUST_BE_DJRS                                        = "42ZB2";
    String LANG_XML_NOT_ALLOWED_DJRS                                = "42ZB3";
    String LANG_NOT_TABLE_FUNCTION                                  = "42ZB4";
    String LANG_NO_COSTING_CONSTRUCTOR                              = "42ZB5";
    String LANG_TABLE_FUNCTION_NOT_ALLOWED                   = "42ZB6";
    String LANG_BAD_TABLE_FUNCTION_PARAM_REF                 = "42ZB7";

	String LANG_NO_SUCH_WINDOW                                         = "42ZC0";
	String LANG_WINDOW_LIMIT_EXCEEDED                                  = "42ZC1";
	String LANG_WINDOW_FUNCTION_CONTEXT_ERROR                          = "42ZC2";

	String LANG_ILLEGAL_UDA_NAME                                  = "42ZC3";
	String LANG_ILLEGAL_UDA_CLASS                                  = "42ZC4";
	String LANG_UDA_WRONG_INPUT_TYPE                                  = "42ZC6";
	String LANG_UDA_WRONG_RETURN_TYPE                                  = "42ZC7";
	String LANG_UDA_INSTANTIATION                                  = "42ZC8";

	String LANG_VARARGS_PARAMETER_STYLE                      = "42ZC9";
	String LANG_DERBY_PARAMETER_STYLE                      = "42ZCA";
	String LANG_VARARGS_RETURN_RESULT_SETS                  = "42ZCB";

    // bad optimizer overrides
    String LANG_BAD_ROW_SOURCE_COUNT                  = "42ZCC";
    String LANG_NOT_LEFT_DEEP                                 = "42ZCD";
    String LANG_UNRESOLVED_ROW_SOURCE                    = "42ZCE";

	//following 3 matches the DB2 sql states
	String LANG_DECLARED_GLOBAL_TEMP_TABLE_ONLY_IN_SESSION_SCHEMA = "428EK";
	String LANG_NOT_ALLOWED_FOR_DECLARED_GLOBAL_TEMP_TABLE = "42995";
	String LANG_LONG_DATA_TYPE_NOT_ALLOWED = "42962";

	String LANG_MULTIPLE_AUTOINCREMENT_COLUMNS                         = "428C1";
	String LANG_TOO_MANY_INDEX_KEY_COLS                                = "54008";
	String LANG_TRIGGER_RECURSION_EXCEEDED                             = "54038";

	//following 1 does not match the DB2 sql state, it is a Derby specific behavior which is not compatible with DB2
	String LANG_OPERATION_NOT_ALLOWED_ON_SESSION_SCHEMA_TABLES = "XCL51.S";

    // error messages for the Lucene plugin
    String LUCENE_NOT_A_STRING_TYPE                                  = "42XBA";
    String LUCENE_NO_PRIMARY_KEY                                  = "42XBB";
    String LUCENE_UNSUPPORTED_TYPE                                  = "42XBC";
    String LUCENE_INVALID_CHARACTER                                  = "42XBD";
    String LUCENE_INDEX_DOES_NOT_EXIST                                  = "42XBE";
    String LUCENE_MUST_OWN_SCHEMA                                  = "42XBF";
    String LUCENE_ALREADY_LOADED                                  = "42XBG";
    String LUCENE_ALREADY_UNLOADED                                  = "42XBH";
    String LUCENE_BAD_INDEX                                               = "42XBI";
    String LUCENE_BAD_COLUMN_NAME                                   = "42XBJ";
    String LUCENE_BAD_VERSION                                           = "42XBK";
    String LUCENE_ENCRYPTED_DB                                           = "42XBL";
    String ARGUMENT_MAY_NOT_BE_NULL                                = "42XBM";
    String LUCENE_FIELD_KEY_CONFLICT                                = "42XBN";
    String LUCENE_DUPLICATE_FIELD_NAME                                = "42XBO";
    
	String RTS_ATTACHED_TO											   = "43X00.U";
	String RTS_BEGIN_SQ_NUMBER										   = "43X01.U";
	String RTS_ANY_RS												   = "43X02.U";
	String RTS_NUM_OPENS											   = "43X03.U";
	String RTS_ROWS_SEEN											   = "43X04.U";
	String RTS_SOURCE_RS											   = "43X05.U";
	String RTS_END_SQ_NUMBER										   = "43X06.U";
	String RTS_OPT_EST_RC											   = "43X07.U";
	String RTS_OPT_EST_COST											   = "43X08.U";
	String RTS_SECONDS												   = "43X09.U";
	String RTS_NODE													   = "43X11.U";
	String RTS_NOT_IMPL												   = "43X12.U";
	String RTS_DELETE_RS_USING										   = "43X13.U";
	String RTS_TABLE_LOCKING										   = "43X14.U";
	String RTS_ROW_LOCKING											   = "43X15.U";
	String RTS_DEFERRED												   = "43X16.U";
	String RTS_ROWS_DELETED											   = "43X17.U";
	String RTS_INDEXES_UPDATED										   = "43X18.U";
	String RTS_DELETE												   = "43X19.U";
	String RTS_DSARS												   = "43X20.U";
	String RTS_ROWS_INPUT											   = "43X21.U";
	String RTS_DISTINCT_SCALAR_AGG									   = "43X22.U";
	String RTS_DISTINCT_SCAN_RS_USING								   = "43X23.U";
	String RTS_DISTINCT_SCAN_RS										   = "43X26.U";
	String RTS_LOCKING												   = "43X27.U";
	String RTS_SCAN_INFO											   = "43X28.U";
	String RTS_DISTINCT_COL											   = "43X29.U";
	String RTS_DISTINCT_COLS										   = "43X30.U";
	String RTS_HASH_TABLE_SIZE										   = "43X31.U";
	String RTS_ROWS_FILTERED										   = "43X32.U";
	String RTS_NEXT_TIME											   = "43X33.U";
	String RTS_START_POSITION										   = "43X34.U";
	String RTS_STOP_POSITION										   = "43X35.U";
	String RTS_SCAN_QUALS											   = "43X36.U";
	String RTS_NEXT_QUALS											   = "43X37.U";
	String RTS_ON_USING												   = "43X38.U";
	String RTS_DISTINCT_SCAN										   = "43X39.U";
	String RTS_SORT_INFO											   = "43X40.U";
	String RTS_GROUPED_AGG_RS										   = "43X41.U";
	String RTS_HAS_DISTINCT_AGG										   = "43X42.U";
	String RTS_IN_SORTED_ORDER										   = "43X43.U";
	String RTS_GROUPED_AGG											   = "43X44.U";
	String RTS_HASH_EXISTS_JOIN										   = "43X45.U";
	String RTS_HASH_EXISTS_JOIN_RS									   = "43X46.U";
	String RTS_HASH_JOIN											   = "43X47.U";
	String RTS_HASH_JOIN_RS											   = "43X48.U";
	String RTS_HASH_LEFT_OJ											   = "43X49.U";
	String RTS_HASH_LEFT_OJ_RS										   = "43X50.U";
	String RTS_HASH_SCAN_RS_USING									   = "43X51.U";
	String RTS_HASH_SCAN_RS											   = "43X52.U";
	String RTS_HASH_KEY												   = "43X53.U";
	String RTS_HASH_KEYS											   = "43X54.U";
	String RTS_HASH_SCAN											   = "43X55.U";
	String RTS_ATTACHED_SQS											   = "43X56.U";
	String RTS_HASH_TABLE_RS										   = "43X57.U";
	String RTS_HASH_TABLE											   = "43X58.U";
	String RTS_ALL													   = "43X59.U";
	String RTS_IRTBR_RS												   = "43X60.U";
	String RTS_COLS_ACCESSED_FROM_HEAP								   = "43X61.U";
	String RTS_FOR_TAB_NAME											   = "43X62.U";
	String RTS_IRTBR												   = "43X63.U";
	String RTS_INSERT_MODE_BULK										   = "43X64.U";
	String RTS_INSERT_MODE_NOT_BULK									   = "43X65.U";
	String RTS_INSERT_MODE_NORMAL									   = "43X66.U";
	String RTS_INSERT_USING											   = "43X67.U";
	String RTS_ROWS_INSERTED										   = "43X68.U";
	String RTS_INSERT												   = "43X69.U";
	String RTS_JOIN													   = "43X70.U";
	String RTS_LKIS_RS												   = "43X71.U";
	String RTS_LOCKING_OPTIMIZER									   = "43X72.U";
	String RTS_TABLE_SCAN											   = "43X73.U";
	String RTS_INDEX_SCAN											   = "43X74.U";
	String RTS_ON													   = "43X75.U";
	String RTS_MATERIALIZED_RS										   = "43X76.U";
	String RTS_TEMP_CONGLOM_CREATE_TIME								   = "43X77.U";
	String RTS_TEMP_CONGLOM_FETCH_TIME								   = "43X78.U";
	String RTS_ROWS_SEEN_LEFT										   = "43X79.U";
	String RTS_ROWS_SEEN_RIGHT										   = "43X80.U";
	String RTS_ROWS_RETURNED										   = "43X81.U";
	String RTS_LEFT_RS												   = "43X82.U";
	String RTS_RIGHT_RS												   = "43X83.U";
	String RTS_NESTED_LOOP_EXISTS_JOIN								   = "43X84.U";
	String RTS_NESTED_LOOP_EXISTS_JOIN_RS							   = "43X85.U";
	String RTS_NESTED_LOOP_JOIN										   = "43X86.U";
	String RTS_NESTED_LOOP_JOIN_RS									   = "43X87.U";
	String RTS_EMPTY_RIGHT_ROWS										   = "43X88.U";
	String RTS_NESTED_LOOP_LEFT_OJ									   = "43X89.U";
	String RTS_NESTED_LOOP_LEFT_OJ_RS								   = "43X90.U";
	String RTS_NORMALIZE_RS											   = "43X91.U";
	String RTS_ONCE_RS												   = "43X92.U";
	String RTS_PR_RS												   = "43X93.U";
	String RTS_RESTRICTION											   = "43X94.U";
	String RTS_PROJECTION											   = "43X95.U";
	String RTS_RESTRICTION_TIME										   = "43X96.U";
	String RTS_PROJECTION_TIME										   = "43X97.U";
	String RTS_PR													   = "43X98.U";
	String RTS_ROW_RS												   = "43X99.U";
	String RTS_RC                                                      = "43X9A.U";
	String RTS_RC_RS                                                   = "43X9B.U";
	String RTS_WINDOW_RS                                               = "43X9C.U";

	String RTS_SCALAR_AGG_RS										   = "43Y00.U";
	String RTS_INDEX_KEY_OPT										   = "43Y01.U";
	String RTS_SCALAR_AGG											   = "43Y02.U";
	String RTS_SCROLL_INSENSITIVE_RS								   = "43Y03.U";
	String RTS_READS_FROM_HASH										   = "43Y04.U";
	String RTS_WRITES_TO_HASH										   = "43Y05.U";
	String RTS_SORT_RS												   = "43Y06.U";
	String RTS_ELIMINATE_DUPS										   = "43Y07.U";
	String RTS_SORT													   = "43Y08.U";
	String RTS_IS_RS_USING											   = "43Y09.U";
	String RTS_TS_RS_FOR											   = "43Y10.U";
	String RTS_ACTUAL_TABLE											   = "43Y11.U";
	String RTS_FETCH_SIZE											   = "43Y12.U";
	String RTS_QUALS												   = "43Y13.U";
	String RTS_UNION_RS												   = "43Y14.U";
	String RTS_UPDATE_RS_USING										   = "43Y16.U";
	String RTS_ROWS_UPDATED											   = "43Y17.U";
	String RTS_VTI_RS												   = "43Y19.U";
	String RTS_VTI													   = "43Y20.U";
	String RTS_MATERIALIZED_SUBQS									   = "43Y21.U";
	String RTS_STATEMENT_NAME										   = "43Y22.U";
	String RTS_STATEMENT_TEXT										   = "43Y23.U";
	String RTS_PARSE_TIME											   = "43Y24.U";
	String RTS_BIND_TIME											   = "43Y25.U";
	String RTS_OPTIMIZE_TIME										   = "43Y26.U";
	String RTS_GENERATE_TIME										   = "43Y27.U";
	String RTS_COMPILE_TIME											   = "43Y28.U";
	String RTS_EXECUTE_TIME											   = "43Y29.U";
	String RTS_BEGIN_COMP_TS										   = "43Y30.U";
	String RTS_END_COMP_TS											   = "43Y31.U";
	String RTS_BEGIN_EXE_TS											   = "43Y32.U";
	String RTS_END_EXE_TS											   = "43Y33.U";
	String RTS_STMT_EXE_PLAN_TXT									   = "43Y44.U";
	String RTS_RUN_TIME												   = "43Y45.U";
	String RTS_INSERT_VTI_RESULT_SET								   = "43Y46.U";
	String RTS_DELETE_VTI_RESULT_SET								   = "43Y47.U";
	String RTS_INSERT_VTI											   = "43Y49.U";
	String RTS_DELETE_VTI											   = "43Y50.U";
	String RTS_DELETE_CASCADE									 	   = "43Y51.U";
	String RTS_DELETE_CASCADE_RS_USING								   = "43Y52.U";
	String RTS_REFACTION_DEPENDENT									   = "43Y53.U";
	String RTS_BEGIN_DEPENDENT_NUMBER								   = "43Y54.U";	
	String RTS_END_DEPENDENT_NUMBER									   = "43Y55.U";	
	String RTS_USER_SUPPLIED_OPTIMIZER_OVERRIDES_FOR_TABLE			   = "43Y56.U";	
	String RTS_USER_SUPPLIED_OPTIMIZER_OVERRIDES_FOR_JOIN			   = "43Y57.U";

	String TI_SQL_TYPE_NAME			= "44X00.U";

	// INTERNAL EXCEPTIONS
	String LANG_UNABLE_TO_GENERATE                                     = "42Z50";
	String LANG_UNAVAILABLE_ACTIVATION_NEED                            = "42Z53";
	String LANG_PARSE_ONLY                                             = "42Z54.U";
	String LANG_STOP_AFTER_PARSING                                     = "42Z55.U";
	String LANG_STOP_AFTER_BINDING                                     = "42Z56.U";
	String LANG_STOP_AFTER_OPTIMIZING                                  = "42Z57.U";
	String LANG_STOP_AFTER_GENERATING                                  = "42Z58.U";

	// PARSER EXCEPTIONS
	String LANG_UNBINDABLE_REWRITE                                     = "X0A00.S";
	
	// EXECUTION EXCEPTIONS
	String LANG_CANT_LOCK_TABLE                                        = "X0X02.S";
	String LANG_TABLE_NOT_FOUND_DURING_EXECUTION                       = "X0X05.S";
	String LANG_CANT_DROP_JAR_ON_DB_CLASS_PATH_DURING_EXECUTION        = "X0X07.S";
	String LANG_USING_CARDINALITY_VIOLATION_DURING_EXECUTION           = "X0X10.S";
	String LANG_NO_ROWS_FROM_USING_DURING_EXECUTION                    = "X0X11.S";
	String LANG_FILE_DOES_NOT_EXIST                                    = "X0X13.S";
	String LANG_NO_CORRESPONDING_S_Q_L_TYPE                            = "X0X57.S";
	String LANG_CURSOR_ALREADY_EXISTS                                  = "X0X60.S";
	String LANG_INDEX_COLUMN_NOT_EQUAL                                 = "X0X61.S";
	String LANG_INCONSISTENT_ROW_LOCATION                              = "X0X62.S";
	String LANG_IO_EXCEPTION                                           = "X0X63.S";
	String LANG_COLUMN_NOT_ORDERABLE_DURING_EXECUTION                  = "X0X67.S";
	String LANG_OBJECT_NOT_FOUND_DURING_EXECUTION                      = "X0X81.S";
	String LANG_NON_KEYED_INDEX                                        = "X0X85.S";
	String LANG_ZERO_INVALID_FOR_R_S_ABSOLUTE                          = "X0X86.S";
	String LANG_NO_CURRENT_ROW_FOR_RELATIVE                            = "X0X87.S";
	String LANG_CANT_INVALIDATE_OPEN_RESULT_SET                        = "X0X95.S";
	String LANG_CANT_CHANGE_ISOLATION_HOLD_CURSOR                      = "X0X03.S";
	//following three for auto-generated keys feature in JDBC3.0
    String INVALID_COLUMN_ARRAY_LENGTH                                 = "X0X0D.S";
	String LANG_INVALID_AUTOGEN_COLUMN_POSITION                        = "X0X0E.S";
	String LANG_INVALID_AUTOGEN_COLUMN_NAME                            = "X0X0F.S";

	String LANG_INDEX_NOT_FOUND_DURING_EXECUTION                       = "X0X99.S";

	// X0Y01 used to be DUPLICATE_KEY_CONSTRAINT
	String LANG_DROP_VIEW_ON_NON_VIEW                                  = "X0Y16.S";
	// String LANG_DROP_SYSTEM_TABLE_ATTEMPTED_DURING_EXECUTION           = "X0Y17.S";
	String LANG_PROVIDER_HAS_DEPENDENT_VIEW                            = "X0Y23.S";
	String LANG_PROVIDER_HAS_DEPENDENT_S_P_S                           = "X0Y24.S";
	String LANG_PROVIDER_HAS_DEPENDENT_OBJECT                          = "X0Y25.S";
	String LANG_INDEX_AND_TABLE_IN_DIFFERENT_SCHEMAS                   = "X0Y26.S";
	String LANG_CREATE_SYSTEM_INDEX_ATTEMPTED                          = "X0Y28.S";
	String LANG_PROVIDER_HAS_DEPENDENT_TABLE                            = "X0Y29.S";
	String LANG_PROVIDER_HAS_DEPENDENT_ALIAS                            = "X0Y30.S";
	String LANG_OBJECT_ALREADY_EXISTS_IN_OBJECT						   = "X0Y32.S";
	String LANG_CREATE_INDEX_NO_TABLE                                  = "X0Y38.S";
	String LANG_INVALID_FK_NO_PK                                       = "X0Y41.S";
	String LANG_INVALID_FK_COL_TYPES_DO_NOT_MATCH                      = "X0Y42.S";
	String LANG_INVALID_FK_DIFFERENT_COL_COUNT                         = "X0Y43.S";
	String LANG_INVALID_FK_NO_REF_KEY                                  = "X0Y44.S";
	String LANG_ADD_FK_CONSTRAINT_VIOLATION                            = "X0Y45.S";
	String LANG_INVALID_FK_NO_REF_TAB                                  = "X0Y46.S";
    String LANG_INVALID_FK_REF_KEY                                     = "X0Y47.S";
	String LANG_SCHEMA_NOT_EMPTY                                       = "X0Y54.S";
	String LANG_INDEX_ROW_COUNT_MISMATCH                               = "X0Y55.S";
	String LANG_INVALID_OPERATION_ON_SYSTEM_TABLE                      = "X0Y56.S";
	String LANG_ADDING_NON_NULL_COLUMN_TO_NON_EMPTY_TABLE              = "X0Y57.S";
	String LANG_ADD_PRIMARY_KEY_FAILED1                                = "X0Y58.S";
	String LANG_ADD_CHECK_CONSTRAINT_FAILED                            = "X0Y59.S";
	String LANG_NULL_DATA_IN_PRIMARY_KEY_OR_UNIQUE_CONSTRAINT      	   = "X0Y63.S";
	String LANG_NULL_DATA_IN_PRIMARY_KEY                               = "X0Y63.S.1";
	String LANG_NO_COMMIT_IN_NESTED_CONNECTION                         = "X0Y66.S";
	String LANG_NO_ROLLBACK_IN_NESTED_CONNECTION                       = "X0Y67.S";
	String LANG_OBJECT_ALREADY_EXISTS                                  = "X0Y68.S";
	String LANG_NO_DDL_IN_TRIGGER                                      = "X0Y69.S";
	String LANG_NO_DML_IN_TRIGGER                                      = "X0Y70.S";
	String LANG_NO_XACT_IN_TRIGGER                                     = "X0Y71.S";
	String LANG_NO_BULK_INSERT_REPLACE_WITH_TRIGGER_DURING_EXECUTION   = "X0Y72.S";
	String LANG_NO_SET_TRAN_ISO_IN_GLOBAL_CONNECTION                   = "X0Y77.S";
	String LANG_INVALID_CALL_TO_EXECUTE_QUERY		                   = "X0Y78.S";
    String MULTIPLE_RESULTS_ON_EXECUTE_QUERY = "X0Y78.S.1";
    String USE_EXECUTE_UPDATE_WITH_NO_RESULTS = "X0Y78.S.2";
	String LANG_INVALID_CALL_TO_EXECUTE_UPDATE		                   = "X0Y79.S";
	String LANG_NULL_DATA_IN_NON_NULL_COLUMN               	   	   	   = "X0Y80.S";
    String LANG_IGNORE_MISSING_INDEX_ROW_DURING_DELETE                 = "X0Y83.S";
    String LANG_TOO_MUCH_CONTENTION_ON_SEQUENCE                 = "X0Y84.T";
	String LANG_UNKNOWN_SEQUENCE_PREALLOCATOR                                = "X0Y85.S";
    String LANG_NOT_A_SEQUENCE_PREALLOCATOR                            = "X0Y85.S.1";
	String LANG_CANT_FLUSH_PREALLOCATOR                                = "X0Y86.S";
	String LANG_BAD_UDA_OR_FUNCTION_NAME                                = "X0Y87.S";
	String LANG_UNKNOWN_TOOL_NAME                                = "X0Y88.S";
    String LANG_UNKNOWN_CUSTOM_TOOL_NAME                              = "X0Y88.S.1";
	String LANG_BAD_OPTIONAL_TOOL_ARGS                                = "X0Y89.S";
	String LANG_CANT_INSTANTIATE_CLASS                                = "X0Y90.S";
    String LANG_SET_CONSTRAINT_NOT_DEFERRABLE                         = "X0Y91.S";
    String LANG_CANNOT_CHANGE_COLUMN_NAMES                         = "X0Y92.S";

	// TEMPORARY EXECUTION RESTRICTIONS

	// Non-SQLSTATE errors 
	String LANG_DOES_NOT_RETURN_ROWS                                   = "XCL01.S";
	String LANG_ACTIVATION_CLOSED                                      = "XCL05.S";
	String LANG_CURSOR_CLOSED                                          = "XCL07.S";
	String LANG_NO_CURRENT_ROW                                         = "XCL08.S";
	String LANG_WRONG_ACTIVATION                                       = "XCL09.S";
	String LANG_OBSOLETE_PARAMETERS                                    = "XCL10.S";
	String LANG_DATA_TYPE_SET_MISMATCH                                 = "XCL12.S";
	String LANG_INVALID_PARAM_POSITION                                 = "XCL13.S";
	String LANG_INVALID_COLUMN_POSITION                                 = "XCL14.S";
	String LANG_INVALID_COMPARE_TO                                     = "XCL15.S";
	String LANG_RESULT_SET_NOT_OPEN                                    = "XCL16.S";
    String LANG_STREAM_RETRIEVED_ALREADY = "XCL18.S";
	String LANG_MISSING_ROW                                            = "XCL19.S";
	String LANG_CANT_UPGRADE_CATALOGS                                  = "XCL20.S";
	String LANG_DDL_IN_BIND                                            = "XCL21.S";
	String LANG_NOT_OUT_PARAM										   = "XCL22.S";
	String LANG_INVALID_S_Q_L_TYPE                                     = "XCL23.S";
	String LANG_PARAMETER_MUST_BE_OUTPUT                               = "XCL24.S";
	String LANG_INVALID_OUT_PARAM_MAP                                  = "XCL25.S";
	String LANG_NOT_OUTPUT_PARAMETER                                   = "XCL26.S";
	String LANG_RETURN_OUTPUT_PARAM_CANNOT_BE_SET                      = "XCL27.S";
	String LANG_STREAMING_COLUMN_I_O_EXCEPTION                         = "XCL30.S";
	String LANG_STATEMENT_CLOSED_NO_REASON							   = "XCL31.S";
	String LANG_STATEMENT_NEEDS_RECOMPILE							   = "XCL32.S";


	
	//delete rule restriction violation errors
	String LANG_CANT_BE_DEPENDENT_ESELF								   = "XCL33.S";
	String LANG_CANT_BE_DEPENDENT_ECYCLE							   = "XCL34.S";
	String LANG_CANT_BE_DEPENDENT_MPATH								   = "XCL35.S";
	String LANG_DELETE_RULE_MUSTBE_ESELF                   			   = "XCL36.S";
	String LANG_DELETE_RULE_MUSTBE_ECASCADE							   = "XCL37.S";
	String LANG_DELETE_RULE_MUSTBE_MPATH							   = "XCL38.S";
	String LANG_DELETE_RULE_CANT_BE_CASCADE_ESELF					   = "XCL39.S";	
	String LANG_DELETE_RULE_CANT_BE_CASCADE_ECYCLE					   = "XCL40.S";	
	String LANG_DELETE_RULE_CANT_BE_CASCADE_MPATH					   = "XCL41.S";	

	String LANG_STATEMENT_UPGRADE_REQUIRED							   = "XCL47.S";

	//truncate table
	String LANG_NO_TRUNCATE_ON_FK_REFERENCE_TABLE                      = "XCL48.S";
	String LANG_NO_TRUNCATE_ON_ENABLED_DELETE_TRIGGERS                 = "XCL49.S";

	String LANG_CANT_UPGRADE_DATABASE                                 = "XCL50.S";

    String LANG_STATEMENT_CANCELLED_OR_TIMED_OUT                       = "XCL52.S";

    /*
	** Language errors that match DB2
	*/

	String	INVALID_SCHEMA_SYS											= "42939";

	/*
	** Modelled on INVALID_SCHEMA_SYS, although not from DB2
	*/
	String  INVALID_ROLE_SYS                                        = "4293A";


	/*
		SQL standard 0A - feature not supported
	*/
	String UNSUPPORTED_PREFIX="0A";

    String NOT_IMPLEMENTED                                          = "0A000.S";
    String JDBC_METHOD_NOT_IMPLEMENTED                              = "0A000.S.1";
    String JDBC_METHOD_NOT_SUPPORTED_BY_SERVER                      = "0A000.S.2";
    String UNSUPPORTED_HOLDABILITY_PROPERTY                         = "0A000.S.3";
    String CANCEL_NOT_SUPPORTED_BY_SERVER                           = "0A000.S.4";
    String SECMECH_NOT_SUPPORTED                                    = "0A000.S.5";
    String DRDA_COMMAND_NOT_IMPLEMENTED                             = "0A000.C.6";
    String DATA_TYPE_NOT_SUPPORTED = "0A000.S.7";



	

	/*
	** Authorization and Authentication
	*/
	String AUTHORIZATION_SPEC_PREFIX="28";
	
	String AUTH_SET_CONNECTION_READ_ONLY_IN_ACTIVE_XACT                = "25501";
	String AUTH_WRITE_WITH_READ_ONLY_CONNECTION                        = "25502";
	String AUTH_DDL_WITH_READ_ONLY_CONNECTION                          = "25503";
	String AUTH_CANNOT_SET_READ_WRITE                                  = "25505";
	String AUTH_INVALID_USER_NAME                                      = "28502";

	/*
	** Dependency manager
	*/
	String DEP_UNABLE_TO_STORE                                         = "XD004.S";

    /*
    ** Connectivity
    */
    //following have statement severity.
    String NO_CURRENT_ROW = "24000";
    // String NULL_TYPE_PARAMETER_MISMATCH = "37000";
    String NO_INPUT_PARAMETERS = "07009";
	String NEED_TO_REGISTER_PARAM = "07004";
    String COLUMN_NOT_FOUND = "S0022";
    //String NO_COMMIT_WHEN_AUTO = "XJ007.S";
    String NO_SAVEPOINT_ROLLBACK_OR_RELEASE_WHEN_AUTO = "XJ008.S";
    String REQUIRES_CALLABLE_STATEMENT = "XJ009.S";
    String NO_SAVEPOINT_WHEN_AUTO = "XJ010.S";
    String NULL_NAME_FOR_SAVEPOINT = "XJ011.S";
    String ALREADY_CLOSED = "XJ012.S";
    String NO_ID_FOR_NAMED_SAVEPOINT = "XJ013.S";
    String NO_NAME_FOR_UNNAMED_SAVEPOINT = "XJ014.S";
    String NOT_FOR_PREPARED_STATEMENT = "XJ016.S";
    String NO_SAVEPOINT_IN_TRIGGER = "XJ017.S";
    String NULL_COLUMN_NAME = "XJ018.S";
    String TYPE_MISMATCH = "XJ020.S";
    String UNSUPPORTED_TYPE = "XJ021.S";
    String SET_STREAM_FAILURE = "XJ022.S";
    String SET_STREAM_INEXACT_LENGTH_DATA = "XJ023.S";
    String NEGATIVE_STREAM_LENGTH = "XJ025.S";
    String NO_AUTO_COMMIT_ON = "XJ030.S";
    String BAD_PROPERTY_VALUE = "XJ042.S";
    String BAD_SCALE_VALUE = "XJ044.S";
    String UNIMPLEMENTED_ISOLATION_LEVEL = "XJ045.S";
    String RESULTSET_RETURN_NOT_ALLOWED = "XJ04B.S";
    String OUTPUT_PARAMS_NOT_ALLOWED = "XJ04C.S";
    String CANNOT_AUTOCOMMIT_XA = "XJ056.S";
    String CANNOT_COMMIT_XA = "XJ057.S";
    String CANNOT_ROLLBACK_XA = "XJ058.S";
    String CANNOT_CLOSE_ACTIVE_XA_CONNECTION = "XJ059.S";
	String CANNOT_HOLD_CURSOR_XA = "XJ05C.S";
    String NOT_ON_FORWARD_ONLY_CURSOR = "XJ061.S";
    String INVALID_FETCH_SIZE = "XJ062.S";
    String INVALID_MAX_ROWS_VALUE = "XJ063.S";
    String INVALID_FETCH_DIRECTION = "XJ064.S";
    String INVALID_ST_FETCH_SIZE = "XJ065.S";
    String INVALID_MAXFIELD_SIZE = "XJ066.S";
    String NULL_SQL_TEXT = "XJ067.S";
    String MIDDLE_OF_BATCH = "XJ068.S";
    String NO_SETXXX_FOR_EXEC_USING = "XJ069.S";
    String BLOB_BAD_POSITION = "XJ070.S";
    String BLOB_NONPOSITIVE_LENGTH = "XJ071.S";
    String BLOB_NULL_PATTERN_OR_SEARCH_STR = "XJ072.S";
    String BLOB_ACCESSED_AFTER_COMMIT = "XJ073.S";
    String INVALID_QUERYTIMEOUT_VALUE = "XJ074.S";
    String BLOB_POSITION_TOO_LARGE = "XJ076.S";
    String BLOB_UNABLE_TO_READ_PATTERN = "XJ077.S";
    String BLOB_INVALID_OFFSET = "XJ078.S";
    String BLOB_LENGTH_TOO_LONG = "XJ079.S";
    String LANG_NUM_PARAMS_INCORRECT = "XJ080.S";
    String INVALID_API_PARAMETER = "XJ081.S";
    String LOB_AS_METHOD_ARGUMENT_OR_RECEIVER = "XJ082.U";
    String UPDATABLE_RESULTSET_API_DISALLOWED = "XJ083.U";
    String COLUMN_NOT_FROM_BASE_TABLE = "XJ084.U";
    String STREAM_EOF = "XJ085.S";
    String CURSOR_NOT_POSITIONED_ON_INSERT_ROW = "XJ086.S";
    String POS_AND_LENGTH_GREATER_THAN_LOB = "XJ087.S";
    
    
    String WASNULL_INVALID = "XJ088.S";
    String CALENDAR_IS_NULL = "XJ090.S";
    String PARAM_NOT_OUT_OR_INOUT = "XJ091.S";
    String BLOB_TOO_LARGE_FOR_CLIENT  = "XJ093.S";
    String ERROR_PRIVILEGED_ACTION = "XJ095.S";
    String SAVEPOINT_NOT_CREATED_BY_CONNECTION = "XJ097.S";
    String BAD_AUTO_GEN_KEY_VALUE = "XJ098.S";
    String READER_UNDER_RUN = "XJ099.S";
    String REGOUTPARAM_SCALE_DOESNT_MATCH_SETTER = "XJ100.S";
    String TABLE_NAME_CANNOT_BE_NULL = "XJ103.S";
    String SHARED_KEY_LENGTH_ERROR = "XJ104.S";
    String DES_KEY_HAS_WRONG_LENGTH = "XJ105.S";
    String CRYPTO_NO_SUCH_PADDING = "XJ106.S";
    String CRYPTO_BAD_PADDING = "XJ107.S";
    String CRYPTO_ILLEGAL_BLOCK_SIZE = "XJ108.S";
    String PRIMARY_TABLE_NAME_IS_NULL = "XJ110.S";
    String FOREIGN_TABLE_NAME_IS_NULL = "XJ111.S";
    String SECURITY_EXCEPTION_ENCOUNTERED = "XJ112.S";    
    String UNABLE_TO_OPEN_FILE = "XJ113.S";
    String CURSOR_INVALID_CURSOR_NAME = "XJ114.S";
    String UNABLE_TO_OPEN_RESULTSET_WITH_REQUESTED_HOLDABILTY = "XJ115.S";
    String TOO_MANY_COMMANDS_FOR_BATCH = "XJ116.S";
    String CANNOT_BATCH_QUERIES = "XJ117.S";
    String QUERY_BATCH_ON_NON_QUERY_STATEMENT = "XJ118.S";
    String CURSOR_INVALID_OPERATION_AT_CURRENT_POSITION = "XJ121.S";
    String CURSOR_NO_UPDATE_CALLS_ON_CURRENT_ROW = "XJ122.S";
    String CURSOR_NOT_ON_CURRENT_OR_INSERT_ROW = "XJ123.S";
    String CURSOR_COLUMN_NOT_UPDATABLE = "XJ124.S";
    String CURSOR_MUST_BE_SCROLLABLE = "XJ125.S";
    String CURSOR_INVALID_FOR_SENSITIVE_DYNAMIC = "XJ126.S";
    //wrapper related
    String UNABLE_TO_UNWRAP = "XJ128.S";
    
    String EXCEEDED_MAX_SECTIONS = "XJ200.S";
    String CURSOR_INVALID_NAME = "XJ202.S";
    String CURSOR_DUPLICATE_NAME = "XJ203.S";
    String UNABLE_TO_OPEN_RS_WITH_REQUESTED_HOLDABILITY = "XJ204.S";
    String NO_TOKENS_IN_SQL_TEXT = "XJ206.S";
    String CANT_USE_EXEC_QUERY_FOR_UPDATE = "XJ207.S";
    String BATCH_NON_ATOMIC_FAILURE = "XJ208.S";
    String STORED_PROC_NOT_INSTALLED = "XJ209.S";
    String STORED_PROC_LOAD_MODULE_NOT_FOUND = "XJ210.S";
    String BATCH_CHAIN_BREAKING_EXCEPTION = "XJ211.S";
    String INVALID_ATTRIBUTE_SYNTAX = "XJ212.S";
    String TRACELEVEL_FORMAT_INVALID = "XJ213.C";
    String IO_ERROR_UPON_LOB_FREE = "XJ214.S";
    String LOB_OBJECT_INVALID = "XJ215.S";
    String LOB_OBJECT_LENGTH_UNKNOWN_YET = "XJ216.S";
    String LOB_LOCATOR_INVALID = "XJ217.S";
    
    //XN - Network-level messages
    String NET_CONNECTION_RESET_NOT_ALLOWED_IN_UNIT_OF_WORK         = "XN001.S";
    String NET_SECKTKN_NOT_RETURNED                                 = "XN002.U";
    String NET_QUERY_PROCESSING_TERMINATED                          = "XN008.S";
    String NET_ERROR_GETTING_BLOB_LENGTH                            = "XN009.S";
    String NET_NULL_PROCEDURE_NAME                                  = "XN010.S";
    String NET_PROCEDURE_NAME_LENGTH_OUT_OF_RANGE                   = "XN011.S";
    String NET_WRONG_XA_VERSION                                     = "XN012.S";
    String NET_INVALID_SCROLL_ORIENTATION                           = "XN013.S";
    String NET_EXCEPTION_ON_READ                                  = "XN014.S";
    String NET_INPUTSTREAM_LENGTH_TOO_SMALL                         = "XN015.S";
    String NET_EXCEPTION_ON_STREAMLEN_VERIFICATION                = "XN016.S";
    String NET_PREMATURE_EOS                                        = "XN017.S";
    String NET_READER_LENGTH_TOO_SMALL                              = "XN018.S";
    String NET_XARETVAL_ERROR                                       = "XN019.S";
    String NET_MARSHALLING_UDT_ERROR                     = "XN020.S";
    String NET_UDT_COERCION_ERROR                               = "XN021.S";
    String NET_WRITE_CHAIN_IS_DIRTY                                 = "XN022.C";
    String NET_LOCATOR_STREAM_PARAMS_NOT_SUPPORTED                  = "XN023.C";
    String NET_DISCONNECT_EXCEPTION_ON_READ                         = "XN024.C";
    
    // XML - Derby-specific XML errors not covered by SQL standard.
    String LANG_MISSING_XML_CLASSES                                 = "XML00";
    String LANG_UNEXPECTED_XML_EXCEPTION                            = "XML01";

    // Used by server for scrollable updatable insensitive result sets
    // to transmit updated state to client. Internal, not seen by user.
    // Has no message in messages.properties as it is never printed.
    String ROW_UPDATED = "rwupd"; 
    // Used by server to signal delete holes to the client. Internal, not 
    // seen by user. Has no message in messages.properties as it is never 
    // printed.
    String ROW_DELETED = "02502";

    //following are session severity.
    String DATABASE_NOT_FOUND = "XJ004.C";
    String MALFORMED_URL = "XJ028.C";
    String BOOT_DATABASE_FAILED = "XJ040.C";
    String CREATE_DATABASE_FAILED = "XJ041.C";
    String CONFLICTING_BOOT_ATTRIBUTES = "XJ048.C";
    String CONFLICTING_CREATE_ATTRIBUTES = "XJ049.C";
	String CONFLICTING_RESTORE_ATTRIBUTES = "XJ081.C";
    String INVALID_ATTRIBUTE = "XJ05B.C";
    
    // Connection exceptions - SQL State class 08
    
    // 08004 SQL State means the server rejected the connection request
    String LOGIN_FAILED = "08004";
    String NET_CONNECT_AUTH_FAILED                          = "08004.C.1";
    String NET_DATABASE_NOT_FOUND                           = "08004.C.2";
    String AUTH_DATABASE_CONNECTION_REFUSED                 = "08004.C.3"; 
    // AUTH_DATABASE_CONNECTION_REFUSED used to be "04501.C"; 
    String AUTH_SHUTDOWN_NOT_DB_OWNER                       = "08004.C.4";
    String AUTH_ENCRYPT_NOT_DB_OWNER                        = "08004.C.5";
    String AUTH_HARD_UPGRADE_NOT_DB_OWNER                   = "08004.C.6";
    // AUTH_x_NOT_DB_OWNER used to be "2850H/I/J.C";
    String CANNOT_CONNECT_TO_DB_IN_SLAVE_MODE               = "08004.C.7";
    String AUTH_REPLICATION_NOT_DB_OWNER                    = "08004.C.8";
    // new state/msg (considered sql state 28101.C not appropriate)
    String AUTH_SHUTDOWN_MISSING_PERMISSION                 = "08004.C.9";
    // new state/msg
    String AUTH_DATABASE_CREATE_EXCEPTION                   = "08004.C.10";
    // new state/msg
    String AUTH_DATABASE_CREATE_MISSING_PERMISSION          = "08004.C.11";
    String NET_CONNECT_SECMEC_INCOMPATIBLE_SCHEME           = "08004.C.12";
    String AUTH_EMPTY_CREDENTIALS                                  = "08004.C.13";
    String AUTH_DECRYPT_NOT_DB_OWNER                        = "08004.C.14";

    // There can be multiple causes for 08003, which according
    // to SQL2003 spec means "connection does not exist"
    // We use a suffix to distinguish them.  Because of the suffix
    // you *must* add a severity code
    String NO_CURRENT_CONNECTION = "08003";
    String NOGETCONN_ON_CLOSED_POOLED_CONNECTION = "08003.C.1";
    String LOB_METHOD_ON_CLOSED_CONNECTION = "08003.C.2";
    String PHYSICAL_CONNECTION_ALREADY_CLOSED = "08003.C.3";

    
    // 08006 means connection exception - connection failure
    String DRDA_CONNECTION_TERMINATED                           = "08006.C";
    String CONNECTION_FAILED_ON_RESET                           = "08006.C.1";
   
    // Use this version of SOCKET_EXCEPTION any time *except* when trying to
    // establish a connection, as the SQLState is different.  When trying
    // to establish a connection, use CONNECT_SOCKET_EXCEPTION.
    String SOCKET_EXCEPTION                                     = "08006.C.2";
    String COMMUNICATION_ERROR                                  = "08006.C.3";
    String CONNECTION_FAILED_ON_DEFERRED_RESET                  = "08006.C.4";
    String NET_INSUFFICIENT_DATA                                = "08006.C.5";
    String NET_LOB_DATA_TOO_LARGE_FOR_JVM                       = "08006.C.6";
    
	String CORE_JDBC_DRIVER_UNREGISTERED                    = "08006.C.8"; // JDBCDriver is not registered with the JDBC driver manager

    // 08001 is specifically about the SQL client not being able to establish
    // a connection with the server.  Should only be used for errors that
    // occur upon attempting to open a connection.
    // NOTE that if the server *rejects* the connection, that's a different
    // SQLState- 08004'
    String CONNECT_REQUIRED_PROPERTY_NOT_SET                    = "08001.C.1";
    String CONNECT_UNABLE_TO_CONNECT_TO_SERVER                  = "08001.C.2";
    // Use this version of socket exception occurs when trying to establish
    // a connection to the server, as the SQL State 08001 indicates failure
    // to establish a connection.  If you aren't trying to connect, just
    // use SOCKET_EXCEPTION
    String CONNECT_SOCKET_EXCEPTION                             = "08001.C.3";
    String CONNECT_UNABLE_TO_OPEN_SOCKET_STREAM                 = "08001.C.4";
    String CONNECT_USERID_LENGTH_OUT_OF_RANGE                   = "08001.C.5";
    String CONNECT_PASSWORD_LENGTH_OUT_OF_RANGE                 = "08001.C.6";
    String CONNECT_USERID_ISNULL                                = "08001.C.7";
    String CONNECT_PASSWORD_ISNULL                              = "08001.C.8";
    String NET_DBNAME_TOO_LONG                                  = "08001.C.9";
    String NET_SECTKN_TOO_LONG                                  = "08001.C.10";
    String NET_USERID_TOO_LONG                                  = "08001.C.11";
    String NET_PASSWORD_TOO_LONG                                = "08001.C.12";
    String NET_EXTNAM_TOO_LONG                                  = "08001.C.13";
    String NET_SRVNAM_TOO_LONG                                  = "08001.C.14";    
    
    // database severity
    String SHUTDOWN_DATABASE = "08006.D";  
    String DROP_DATABASE = "08006.D.1";
        
    //the following 2 exceptions are internal and never get seen by the user.
    String CLOSE_REQUEST = "close.C.1"; // no message in messages.properties as it is never printed

    //this one had no sqlstate associated with it.
    String NORMAL_CLOSE = "XXXXX.C.6";

    //following are system severity.
    String CLOUDSCAPE_SYSTEM_SHUTDOWN = "XJ015.M";

    //following are warning severity.
    String DATABASE_EXISTS = "01J01";
    String NO_SCROLL_SENSITIVE_CURSORS = "01J02";
	String LANG_TYPE_NOT_SERIALIZABLE = "01J04";
	String UPGRADE_SPSRECOMPILEFAILED = "01J05";
    String QUERY_NOT_QUALIFIED_FOR_UPDATABLE_RESULTSET = "01J06";
    String HOLDABLE_RESULT_SET_NOT_AVAILABLE = "01J07";
    String INVALID_RESULTSET_TYPE = "01J08";
    String SCROLL_SENSITIVE_NOT_SUPPORTED = "01J10";
    String UNABLE_TO_OBTAIN_MESSAGE_TEXT_FROM_SERVER  = "01J12";
    String NUMBER_OF_ROWS_TOO_LARGE_FOR_INT = "01J13";
	String SQL_AUTHORIZATION_WITH_NO_AUTHENTICATION = "01J14";
	String PASSWORD_EXPIRES_SOON = "01J15";
	String DBO_PASSWORD_EXPIRES_SOON = "01J16";
    String AUTH_ENCRYPT_ALREADY_BOOTED = "01J17";

    String CURSOR_OPERATION_CONFLICT = "01001";



    //following are no applicable severity
    String JAVA_EXCEPTION = "XJ001.U";
    String NO_UPGRADE = "XJ050.U";
        
    /*
     ** Messages whose SQL states are prescribed by DRDA
     */
    String DRDA_NO_AUTOCOMMIT_UNDER_XA                              = "2D521.S.1";
    String DRDA_INVALID_XA_STATE_ON_COMMIT_OR_ROLLBACK              = "2D521.S.2"; 
    String DRDA_CURSOR_NOT_OPEN                                     = "24501.S";

    // 58009 means connection is terminated by a DRDA-protocol error.  This can be caused by any number
    // of reasons, so this SQL State has a lot of instances. Exceptions that are 
    // not protocol related, e.g. SocketException, IOException etc should use 
    // SQLState 8006. 
    String NET_SQLCDTA_INVALID_FOR_RDBCOLID                         = "58009.C.7";
    String NET_SQLCDTA_INVALID_FOR_PKGID                            = "58009.C.8";
    String NET_PGNAMCSN_INVALID_AT_SQLAM                            = "58009.C.9";
    String NET_VCM_VCS_LENGTHS_INVALID                              = "58009.C.10";
    String NET_ENCODING_NOT_SUPPORTED                               = "58009.C.11";
    String NET_NOT_EXPECTED_CODEPOINT                               = "58009.C.12";
    String NET_DDM_COLLECTION_TOO_SMALL                             = "58009.C.13";
    String NET_COLLECTION_STACK_NOT_EMPTY                           = "58009.C.14";
    String NET_DSS_NOT_ZERO                                         = "58009.C.15";
    String NET_DSS_CHAINED_WITH_SAME_ID                             = "58009.C.16";
    String NET_PREMATURE_EOS_DISCONNECT                             = "58009.C.17";
    String NET_INVALID_FDOCA_ID                                     = "58009.C.18";
    String NET_SECTKN_NOT_RETURNED                                  = "58009.C.19";
    String NET_NVCM_NVCS_BOTH_NON_NULL                              = "58009.C.20";
    String NET_SQLCDTA_INVALID_FOR_RDBNAM                           = "58009.C.21";

    String DRDA_MGRLVLRM                                            = "58010.C";
    String DRDA_DDM_COMMAND_NOT_SUPPORTED                           = "58014.C";
    String DRDA_DDM_OBJECT_NOT_SUPPORTED                            = "58015.C";
    String DRDA_DDM_PARAM_NOT_SUPPORTED                             = "58016.C";
    String DRDA_DDM_PARAMVAL_NOT_SUPPORTED                          = "58017.C";
    String DRDA_NO_AVAIL_CODEPAGE_CONVERSION                        = "57017.C";
    
	String UU_UNKNOWN_PERMISSION									= "XCZ00.S";
	String UU_UNKNOWN_USER											= "XCZ01.S";
	String UU_INVALID_PARAMETER										= "XCZ02.S";

	/*
	** SQL Java DDL 46xxx
	** SQLJ jar file support
	*/
	String SQLJ_INVALID_JAR				= "46001";
	String SQLJ_SIGNATURE_INVALID	    		= "46J01";
	String SQLJ_SIGNATURE_PARAMETER_COUNT	    = "46J02";

	/*
	** Import/Export
	*/
	String CONNECTION_NULL                                         ="XIE01.S";
	String DATA_AFTER_STOP_DELIMITER                               ="XIE03.S";
	String DATA_FILE_NOT_FOUND                                     ="XIE04.S";
	String DATA_FILE_NULL                                          ="XIE05.S";
	String ENTITY_NAME_MISSING                                     ="XIE06.S";
	String FIELD_IS_RECORD_SEPERATOR_SUBSET                        ="XIE07.S";
    String INVALID_COLUMN_NAME                                     ="XIE08.S";
	String INVALID_COLUMN_NUMBER                                   ="XIE09.S";
	String UNSUPPORTED_COLUMN_TYPE                                 ="XIE0B.S";
	String RECORD_SEPERATOR_MISSING                                ="XIE0D.S";
	String UNEXPECTED_END_OF_FILE                                  ="XIE0E.S";
	String ERROR_WRITING_DATA                                      ="XIE0I.S";
	String DELIMITERS_ARE_NOT_MUTUALLY_EXCLUSIVE                   ="XIE0J.S";
	String PERIOD_AS_CHAR_DELIMITER_NOT_ALLOWED                    ="XIE0K.S";
	String TABLE_NOT_FOUND                                         ="XIE0M.S";
	String IMPORTFILE_HAS_INVALID_HEXSTRING                        ="XIE0N.S";
	String LOB_DATA_FILE_NOT_FOUND                                 ="XIE0P.S";
	String LOB_DATA_FILE_NULL                                      ="XIE0Q.S";
	String UNEXPECTED_IMPORT_ERROR                       ="XIE0R.S";
	String DATA_FILE_EXISTS		                     ="XIE0S.S";
	String LOB_DATA_FILE_EXISTS                          ="XIE0T.S";



    /*
    ** Security XK...
    */
    String POLICY_NOT_RELOADED                                     ="XK000.S";
    String NO_SUCH_USER                                                  ="XK001.S";

    /*
    ** Replication XRExx
    */
    String LOGMODULE_DOES_NOT_SUPPORT_REPLICATION                  = "XRE00";
    String REPLICATION_LOG_CORRUPTED                               = "XRE01";
    String REPLICATION_MASTER_SLAVE_VERSION_MISMATCH               = "XRE02";
    String REPLICATION_UNEXPECTED_EXCEPTION                        = "XRE03";
    String REPLICATION_CONNECTION_EXCEPTION                        = "XRE04.C.1";
    String REPLICATION_CONNECTION_LOST                             = "XRE04.C.2";
    String REPLICATION_LOG_OUT_OF_SYNCH                            = "XRE05.C";
    String REPLICATION_MASTER_TIMED_OUT                            = "XRE06";
    String REPLICATION_NOT_IN_MASTER_MODE                          = "XRE07";
    String REPLICATION_SLAVE_STARTED_OK                            = "XRE08";
    String CANNOT_START_SLAVE_ALREADY_BOOTED                       = "XRE09.C";
    String REPLICATION_CONFLICTING_ATTRIBUTES                      = "XRE10";
    String REPLICATION_DB_NOT_BOOTED                               = "XRE11.C";
    String REPLICATION_UNEXPECTED_MESSAGEID                        = "XRE12";
    String REPLICATION_FAILOVER_SUCCESSFUL                         = "XRE20.D";
    String REPLICATION_FAILOVER_UNSUCCESSFUL                       = "XRE21.C";
    String REPLICATION_MASTER_ALREADY_BOOTED                       = "XRE22.C";
    String REPLICATION_UNLOGGED_OPERATIONS_IN_PROGRESS             = "XRE23";
    String REPLICATION_NOT_IN_SLAVE_MODE                           = "XRE40";
    String SLAVE_OPERATION_DENIED_WHILE_CONNECTED                  = "XRE41.C";
    String REPLICATION_SLAVE_SHUTDOWN_OK                           = "XRE42.C";
    String REPLICATION_STOPSLAVE_NOT_INITIATED                     = "XRE43";
}