package it.unibz.inf.mixer_main.consts;

public interface Labels {
	static final String SEP = "#";
	
	public static final String EX_TIME = "execution_time";
	public static final String RES_TRAVERSAL_TIME = "resultset_traversal_time";
	public static final String NUM_RESULTS = "num_results";
	public static final String RW_TIME = "rewriting_time";
	public static final String UNF_TIME = "unfolding_time";
	public static final String RW_UCQ_SIZE = "rewritingUCQ_size";
	public static final String UNF_UCQ_SIZE = "unfoldingUCQ_size";
	public static final String RUN = "run";
	public static final String MIX_TIME = "mix_time";
	
	// Struct stats
	public static final String STRUCT_STATS = "struct_stats";
	public static final String DLOG_UNF_MIN_Q_SIZE = "dlog_unf_min_q_size";
	public static final String DLOG_UNF_AVG_Q_SIZE = "dlog_unf_avg_q_size";
	public static final String DLOG_UNF_MAX_Q_SIZE = "dlog_unf_max_q_size";
	public static final String DLOG_RW_MIN_Q_SIZE = "dlog_rw_min_q_size";
	public static final String DLOG_RW_AVG_Q_SIZE = "dlog_rw_avg_q_size";
	public static final String DLOG_RW_MAX_Q_SIZE = "dlog_rw_max_q_size";
	
	// Log info 
	public static final String LOG_INFO = "log_info";
	public static final String SPARQL_INPUT = "sparql_input";
	public static final String SPARQL_RW = "sparql_rw";
	public static final String DLOG_RW = "dlog_rw";
	public static final String DLOG_UNF = "dlog_unf";
	public static final String SQL_UNF = "sql_unf";
	
};
