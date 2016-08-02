/*
 * #%L
 * mixer-mastro
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

	@Override
	public int getUnfoldingSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRewritingSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSQLCharsNumber() {
	    // TODO Auto-generated method stub
	    return 0;
	}
	
}
