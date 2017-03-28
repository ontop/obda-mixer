package it.unibz.inf.mixer_shell.core.factory.throw_away;

import it.unbz.inf.utils.mixer_shell.utils.utils_datatypes.string_manipulation.Template;

public class D2RQMaker implements CommandMaker {
    
    private Template cmd = new Template("? ? '?'"); // cmd map-file query
    private final static String EXEC = "d2rq-query.sh";
    
    D2RQMaker() {}

    @Override
    public String make(String execPath, String query, String mapping) {
	
	cmd.setNthPlaceholder(1, execPath + "/" + EXEC );
	cmd.setNthPlaceholder(2, mapping);
	cmd.setNthPlaceholder(3, query);
	return cmd.getFilled();
    }
   

}
