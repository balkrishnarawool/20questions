# 20 Questions Game

This is a fun "20 Questions" game where the computer tries to guess the famous person you're thinking of by asking strategic yes/no questions.

## It uses

- Spring AI 2.0
- SpringBoot 4.0.3
- Java 25
- Maven 3.6+
- [Ollama](https://ollama.ai/) running locally or mocked
- Llama model installed in Ollama (if running locally)

## Spring-AI features used

- Integration with Llama/ Ollama
- ChatClient
- Prompt separation: Different system and user prompts
- Memory for chat (MessageWindowChatMemory with MessageChatMemoryAdvisor)
- Type-safe entity conversion
- Spring Retry integration (Although not a Spring AI feature, it is used for handling invalid JSON responses from LLM)

## How to run

There are two modules `twenty-questions-game` and `mock-ollama-server`. Both are SpringBoot apps.

Run `twenty-questions-game` and optionally run `mock-ollama-server`.

See [twenty-questions-game](twenty-questions-game/README.md) and [mock-ollama-server](mock-ollama-server/README.md) for more details.


