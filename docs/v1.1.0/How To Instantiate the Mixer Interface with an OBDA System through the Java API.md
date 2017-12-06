# How To Instantiate the Mixer Interface with an OBDA System through the Java API

**First Step: Create a new Maven Module**

obda-mixer can test all OBDA systems that provide a Java API for query answering. To do so, it suffices to provide an implementation to the abstract method of the class `Mixer`. The first step is to create a Maven module, and then add a dependency to the _mixer-interface_ project in order to allow inheritance from the class _Mixer_. As an example, we report the pom file of the maven module _mixer-ontop_, containing the concrete class that implements the methods in class _Mixer_ through -ontop- API calls

~~~~~~~~
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>it.unibz.inf.mixer</groupId>
    <artifactId>mixer</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </parent>
  <artifactId>mixer-ontop</artifactId>
  <groupId>it.unibz.inf.mixer</groupId>	
  <version>0.0.1-SNAPSHOT</version>
  
  <dependencies>
	<!-- Dependency to the mixer-interface -->
	<dependency>
  		<artifactId>mixer-interface</artifactId>
  		<groupId>it.unibz.inf.mixer</groupId>
  		<version>0.0.1-SNAPSHOT</version>
  	</dependency>
  	
  	<!-- ONTOP -->
	<dependency>
		<groupId>it.unibz.inf.ontop</groupId>
		<artifactId>ontop-quest-owlapi3</artifactId>
		<version>1.13.1-BENCHMARK</version>
	</dependency>
  </dependencies>
</project>
~~~~~~~~

**Second Step: Provide Implementations to the Public Interface of _Mixer_**

For example, the method _Mixer.executeQuery()_ is implemented through the -ontop- Java API as:

~~~~~~~
	@Override
	public Object executeQuery(String query) {
		QuestOWLResultSet rs = null;
		try {
			if(conn == null) conn = new QuestOWLConnection(reasoner.getQuestInstance().getConnection());
			QuestOWLStatement st = conn.createStatement();
			rs = st.executeTuple(query);			
		} catch (OBDAException | OWLException e) {
			e.printStackTrace();
		} 
		return rs;
	}
~~~~~~~

**Third Step: Add your OBDA System to the Options**

You need to enrich the set of values allowed for the --obda option. This is done by modifying a single line in the _MixerMain_ class, contained in the _mixer-main_ project. For example:

~~~~~~~~~~~~~~
// Command-line option deciding which Mixer implementation should be used
	private StringOptionWithRange optOBDASystem = new StringOptionWithRange("--obda", "The OBDA system under test", "Mixer", "ontop", new StringRange("[ontop]"));
~~~~~~~~~~~~~~

becomes

~~~~~~~~~~~~~~~~~~
// Command-line option deciding which Mixer implementation should be used
	private StringOptionWithRange optOBDASystem = new StringOptionWithRange("--obda", "The OBDA system under test", "Mixer", "ontop", new StringRange("[ontop,my_obda_system]"));
~~~~~~~~~~~~~~~~~~

The next step is to tell the Mixer what concrete class corresponds to the freshly added option "_--obda=my_obda_system_". For example, if the implementation of the _Mixer_ class is called _MixerMyOBDA_, then the method

~~~~~~~~~~~~
	/** Modify this method to add other systems **/
	private void instantiateMixer(Conf configuration) {
		String system = optOBDASystem.getValue();
		
		if( system.equals("ontop") ){
			mixer = new MixerOntop(configuration);
		}		
	}
~~~~~~~~~~~~

contained in class _MixerMain_ should be changed in

~~~~~~~~~~~~
	/** Modify this method to add other systems **/
	private void instantiateMixer(Conf configuration) {
		String system = optOBDASystem.getValue();
		
		if( system.equals("ontop") ){
			mixer = new MixerOntop(configuration);
		}
		else if( system.equals("my_obda_system") ){
			mixer = new MixerMyOBDA(configuration);
		}
	}
~~~~~~~~~~~~

After [setting up the configuration file](Build and Run the Mixer), you can tell the obda-mixer to run tests over the newly added OBDA system by the command-line option

~~~~~~
--obda=my_obda_system
~~~~~~ 
