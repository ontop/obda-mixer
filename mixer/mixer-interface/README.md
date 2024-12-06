# mixer-interface

Defines interfaces and classes for implementing obda-mixer plugins.

To build a plugin, import this module as compile dependency and implement either the Mixer or the QuerySelector interface. See corresponding Javadoc for further information.   

```mermaid
classDiagram
%% Plugins
class Plugins {
    <<utility>>
    list(class...)$ Set< String >
    describe(name)$ Map< String, String > 
    create(name)$ Plugin
}
class Plugin {
    <<interface>>
    init(config)
    close()
}
class AbstractPlugin {
    <<abstract>>
    ... basic init/close logic ...
}
class QuerySelector {
    <<interface>>
    nextQueryMix() List< Query >
}
class Mixer {
    <<interface>>
    prepare(query) QueryExecution
}
class AbstractMixer {
    <<abstract>>
    ... closing of pending executions ...        
}
class QueryExecution {
    <<interface>>
    execute(handler)
    close()
}
Plugins ..> Plugin : create
Plugin <|.. AbstractPlugin
Plugin <|-- QuerySelector
Plugin <|-- Mixer
Mixer <|.. AbstractMixer
AbstractPlugin <|-- AbstractMixer
Handler <.. QueryExecution  : use
Mixer ..> Query : use
Mixer ..> QueryExecution : create
QuerySelector ..> Query : create

%% Handlers
class Handlers {
    <<utility>>
    nil()$ Handler
    validator()$ Handler
    logger()$ Handler
    logger(sink)$ Handler
}
class Handler {
    <<interface>>
    onSubmit()
    onStartResults()
    onSolutionIRIBinding(variable, iri)
    onSolutionBNodeBinding(variable, id)
    onSolutionLiteralBinding(variable, label, datatypeIri, lang)
    onSolutionSQLBinding(variable, value, type)
    onSolutionCompleted()
    onEndResults(numSolutions)
    onMetadata(attribute, value)
}
class AbstractHandler {
    <<abstract>>
    ... no-op callbacks ...
}
Handlers ..> Handler : create
Handler <|.. AbstractHandler

%% enums and value objects
class QueryLanguage {
    <<enum>>
    SPARQL
    SQL
}
class Template {
    apply( ... ) String
}
class Query {
    id : String
    executionId : String
    template : Template
    placeholderFillers : List< String >
    filledTemplate : String
    language : QueryLanguage
    timeout : int
    resultSorted : boolean
    resultIgnored : boolean
    attempt : int
}
```

