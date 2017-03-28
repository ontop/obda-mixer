package it.unibz.inf.mixer_shell.core.factory.throw_away;

import it.unbz.inf.utils.mixer_shell.utils.utils_datatypes.string_manipulation.Template;


/**
 * 
 * @author Davide Lanti
 *
 *
 * java -cp .:morph-rdb.jar:lib/* es.upm.fi.dia.oeg.morph.r2rml.rdb.engine.MorphRDBRunner examples-mysql example1-batch-mysql.morph.properties
 */
public class MorphMaker implements CommandMaker {

    private Template cmd = new Template("? ? '?'"); // cmd map-file query
    
    MorphMaker(){
	// TODO
    }

    @Override
    public String make(String execPath, String query, String mapping) {
	// TODO Auto-generated method stub
	return null;
    }
    

}
