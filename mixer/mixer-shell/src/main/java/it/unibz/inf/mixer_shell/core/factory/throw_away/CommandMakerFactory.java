package it.unibz.inf.mixer_shell.core.factory.throw_away;

public abstract class CommandMakerFactory {
    
    private static CommandMaker getD2RQMaker(){
	return new D2RQMaker();
    }
    
    private static CommandMaker getMorphMaker(){
	return new MorphMaker();
    }
    
    private static CommandMaker getOntopMaker(){
	return new OntopMaker();
    }
    
    public static CommandMaker getCommandMaker(String type) throws UnsupportedOBDASystemException{
	CommandMaker result = null;
	switch(type){
	case "d2rq" : 
	    result = getD2RQMaker();
	    break;
	case "morph" :
	    result = getMorphMaker();
	    break;
	case "ontop" : 
	    result = getOntopMaker();
	    break;
	default : 
	    throw new UnsupportedOBDASystemException("Unsupported OBDA System "+ type);
	}
	return result;
    }
};
