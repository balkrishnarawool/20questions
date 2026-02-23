package com.mock.ollama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MockOllamaApplication {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🎭 MOCK OLLAMA SERVER");
        System.out.println("=".repeat(60));
        System.out.println("📍 Starting on: http://localhost:11434");
        System.out.println("🎯 Endpoint: POST /api/chat");
        System.out.println("💡 Returns instant mock responses for 20 Questions game");
        System.out.println("=".repeat(60));
        System.out.println("\n⚠️  Make sure real Ollama is stopped!");
        System.out.println("✅ Ready to receive requests!\n");
        
        SpringApplication.run(MockOllamaApplication.class, args);
    }
}
