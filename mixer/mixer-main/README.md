# mixer-main

Provides main class (**MixerMain**) for running obda-mixer.

This module:
* builds on mixer-interface and its plugin mechanism for instantiating query mixes (**QuerySelector**) and for evaluating them (**Mixer**);
* includes a built-in plugin for instantiating mixes from a CSV file (**QuerySelectorCSV**);
* includes logics to manage query evaluation statistics (**StatisticsManager**), supporting their collection (**StatisticsCollector**) at different global/client/mix/query scopes (**StatisticsScope**), as well the possibility of including data from external log files (e.g., Ontop one);
* includes a simple built-in mechanism to parse the command line (**Option**), supporting extra options defined in metadata of plugins available at runtime;
* supports the generation of a "really-executable" fat-jar embedding compiled code and an executable startup shell script (**src/assembly/fatjar-embedded-run-script.sh**).

Plugins coming with obda-mixer are included as provided dependencies to facilitate testing. These are promoted runtime dependencies when generating the obda-mixer fat jar.

```mermaid
classDiagram
    namespace mixer-interface {
        class Mixer { <<interface>> }
        class AbstractMixer { <<abstract>> }
        class AbstractPlugin { <<abstract>> }
        class QuerySelector { <<interface>> }
    }
    namespace mixer-main {
        class MixerMain {
            main(args...)$
        }
        class MixerThread["MixerThread : Thread"] {
            run()
        }
        class QuerySelectorCSV {
        }
        class MixerOptions {
            optMixer: Option< String >
            optSelector: Option< String >
            ... other core options ...
            ... loading of plugin options ...
            parse(args...)
        }
    }
    namespace mixer-main_utils {
        class Option["Option"]
    }
    namespace mixer-main_statistics {
        class StatisticsCollector["StatisticsCollector"] {
            has(attribute) boolean
            get(attribute[, Class< T >]) T
            set(attribute, value[, merger])
            add(attribute, increment)
        }
        class StatisticsManager["StatisticsManager"] {
            getCollector(scope): StatisticsCollector
            importJson(in, filter, prefix, markers...)
            write(file) / writeText(out) / writeJson(out)
        }
        class StatisticsScope["StatisticsScope"] {
            <<value object>>
            clientId : OptionalInt
            mixId : OptionalInt
            queryId : OptionalInt
            global()$ StatisticsScope
            forClient(clientId)$ StatisticsScope
            forMix(clientId, mixId)$ StatisticsScope
            forQuery(clientId, mixId, queryId)$ StatisticsScope
            fromString(string)$ StatisticsScope ...
        }
    }
    QuerySelector <|.. QuerySelectorCSV
    AbstractPlugin <|.. QuerySelectorCSV
    MixerThread <.. MixerMain  : create
    MixerOptions <.. MixerMain : use
    Mixer <-- MixerThread
    QuerySelector <-- MixerThread
    MixerThread --> StatisticsManager
    StatisticsManager *..> StatisticsCollector
    StatisticsManager ..> StatisticsScope
    Option <.. MixerOptions
    MixerMain ..> StatisticsManager : use
```

