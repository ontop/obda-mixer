# mixer-shell

Implements the **MixerShell** plugin providing an implementation of Mixer that evaluates query by running a configurable shell command. 

This module is included as either provided (for testing) or runtime (for packaging) dependency of mixer-main.

```mermaid
classDiagram
    namespace mixer-interface {
        class AbstractMixer { <<abstract>> }
    }
    namespace mixer-shell {
        class MixerShell { }
        class LogToFile { <<auxiliary>> }
    }
    AbstractMixer <|-- MixerShell
    MixerShell ..> LogToFile : use
```

