import it.unibz.inf.mixer_interface.configuration.Conf;
import it.unibz.inf.mixer_interface.core.Mixer;


public class MixerMastro extends Mixer {

	public MixerMastro(Conf configuration) {
		super(configuration);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getRewritingTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getUnfoldingTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getUnfolding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRewriting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rewritingOFF() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rewritingON() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object executeQuery(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object executeQuery(String query, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int traverseResultSet(Object resultSet) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
