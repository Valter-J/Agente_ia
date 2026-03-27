package com.agenteia.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
 
//  * Agente de Base de Conhecimento com IA (Gemini)
//  * Projeto: Estudo de Caso - Inteligência Artificial
// * Curso: Ciência da Computação
// * Como funciona:
//  *  1. O usuário digita uma pergunta no terminal
//  *  2. O programa monta uma requisição HTTP para a API do Gemini
//  * 3. O Gemini (modelo de linguagem / ML) processa e responde
//   4. O programa exibe a resposta ao usuário

public class AgenteConhecimento {

private static final String API_KEY = "AIzaSyApXq4CtC2iMuSkHZcJHQWPsUal8QTtx0M";
 
    // Endpoint da API do Gemini
   private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/"
        + "gemini-2.5-flash:generateContent?key=" + API_KEY;
 
    // Prompt de sistema: define o "papel" do agente
    private static final String PERSONA =
            "Você é um assistente especialista em Ciência da Computação. "
          + "Responda dúvidas sobre estruturas de dados, algoritmos, programação orientada a objetos "
          + "e fundamentos de computação. Seja didático, claro e use exemplos quando possível. "
          + "Responda sempre em português.";
 
    // =====================================================
 
    public static void main(String[] args) throws Exception {
 
        Scanner scanner = new Scanner(System.in);
        HttpClient client = HttpClient.newHttpClient();
 
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   AGENTE DE CONHECIMENTO - Ciência da Comp.  ║");
        System.out.println("║       Powered by Google Gemini (IA / ML)     ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println("Digite sua dúvida sobre algoritmos, estruturas");
        System.out.println("de dados ou programação. Digite 'sair' para encerrar.");
        System.out.println("------------------------------------------------\n");
 
        while (true) {
            System.out.print("Você: ");
            String pergunta = scanner.nextLine().trim();
 
            if (pergunta.equalsIgnoreCase("sair")) {
                System.out.println("\nAgente encerrado. Bons estudos!");
                break;
            }
 
            if (pergunta.isEmpty()) {
                System.out.println("(Por favor, digite uma pergunta)\n");
                continue;
            }
 
            System.out.println("\nAgente pensando...\n");
 
            String resposta = consultarGemini(client, pergunta);
            System.out.println("Agente: " + resposta);
            System.out.println("\n------------------------------------------------\n");
        }
 
        scanner.close();
    }
 
    /**
     * Envia a pergunta para a API do Gemini e retorna a resposta como texto.
     *
     * A requisição segue o formato JSON esperado pela API:
     * {
     *   "contents": [
     *     { "role": "user", "parts": [{ "text": "..." }] }
     *   ],
     *   "systemInstruction": { "parts": [{ "text": "..." }] }
     * }
     */
    private static String consultarGemini(HttpClient client, String pergunta) throws Exception {
 
        // Monta o corpo JSON da requisição
        // Escapamos as aspas e quebras de linha para não quebrar o JSON
        String perguntaSegura = pergunta.replace("\\", "\\\\").replace("\"", "\\\"");
        String personaSegura  = PERSONA.replace("\\", "\\\\").replace("\"", "\\\"");
 
        String json = "{"
            + "\"systemInstruction\": {"
            + "  \"parts\": [{\"text\": \"" + personaSegura + "\"}]"
            + "},"
            + "\"contents\": ["
            + "  {\"role\": \"user\","
            + "   \"parts\": [{\"text\": \"" + perguntaSegura + "\"}]}"
            + "]"
            + "}";
 
        // Monta e envia a requisição HTTP POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
 
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
 
        // Extrai o texto da resposta JSON
        return extrairTextoResposta(response.body());
    }
 
    /**
     * Extrai apenas o texto da resposta JSON do Gemini.
     *
     * A resposta tem essa estrutura:
     * {
     *   "candidates": [{
     *     "content": {
     *       "parts": [{ "text": "RESPOSTA AQUI" }]
     *     }
     *   }]
     * }
     *
     * Fazemos a extração manualmente para não precisar de bibliotecas externas.
     */
    private static String extrairTextoResposta(String json) {
        // Verificar se a API retornou erro (ex: chave inválida)
        if (json.contains("\"error\"")) {
            if (json.contains("API_KEY_INVALID") || json.contains("API key not valid")) {
                return "[ERRO] Chave de API inválida. Verifique o valor de API_KEY no código.";
            }
            return "[ERRO] Problema ao consultar a API: " + json;
        }
 
        // Busca o campo "text" dentro de "parts"
        String marcador = "\"text\": \"";
        int inicio = json.indexOf(marcador);
        if (inicio == -1) {
            return "[ERRO] Resposta inesperada da API: " + json;
        }
 
        inicio += marcador.length();
        int fim = json.indexOf("\"\n", inicio);
 
        // Busca alternativa caso o JSON esteja numa linha só
        if (fim == -1) {
            fim = json.indexOf("\"}", inicio);
        }
        if (fim == -1) {
            fim = json.length() - 1;
        }
 
        String texto = json.substring(inicio, fim);
 
        // Decodifica caracteres escapados comuns no JSON
        texto = texto.replace("\\n", "\n")
                     .replace("\\t", "\t")
                     .replace("\\\"", "\"")
                     .replace("\\\\", "\\");
 
        return texto.trim();
    }
}



