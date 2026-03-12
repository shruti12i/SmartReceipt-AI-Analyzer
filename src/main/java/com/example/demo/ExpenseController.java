package com.example.demo;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS}) 
public class ExpenseController {

 
@Value("${ocr.api.key}")
private String OCR_API_KEY;
    private final String OCR_API_URL = "https://api.ocr.space/parse/image";

    @PostMapping(value = "/analyze", produces = "application/json")
    public Map<String, String> analyzeReceipt(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = new LinkedHashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("status", "error");
                result.put("message", "No image was uploaded.");
                return result;
            }

            // 1. THE FIX: Convert the image file into a Base64 text string
            String base64Encoded = Base64.getEncoder().encodeToString(file.getBytes());
            String base64Image = "data:" + file.getContentType() + ";base64," + base64Encoded;

            // 2. Send the string using the 'base64Image' parameter instead of 'file'
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("apikey", OCR_API_KEY);
            body.add("base64Image", base64Image);
            body.add("OCREngine", "2");

            // 3. Send as standard URL Encoded form data (bypasses the multipart bug)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(OCR_API_URL, requestEntity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            JsonNode parsedResults = root.path("ParsedResults");
            if (parsedResults.isArray() && parsedResults.size() > 0) {
                
                String extractedText = parsedResults.get(0).path("ParsedText").asText();
                String total = findTotal(extractedText);

                result.put("status", "success");
                result.put("total_found", total);
                result.put("raw_text", extractedText.replace("\n", " | "));
                return result;
            } else {
                result.put("status", "error");
                result.put("message", "API Response: " + response.getBody());
                return result;
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Server error: " + e.getMessage());
            return result;
        }
    }

 private String findTotal(String text) {
    // 1. Prepare text for scanning (remove colons, make uppercase)
    String cleanText = text.toUpperCase().replace(":", " ");
    String[] lines = cleanText.split("\\n|\\|"); 

    // 2. PRIMARY STRATEGY: Key-Value Search (Bottom-Up)
    // Most totals are at the end. We look for the word 'Total' and grab the nearest number.
    String[] totalKeywords = {"TOTAL", "GRAND TOTAL", "AMOUNT DUE", "NET TO PAY", "DUE"};
    
    for (int i = lines.length - 1; i >= 0; i--) {
        String line = lines[i].trim();
        for (String word : totalKeywords) {
            if (line.contains(word)) {
                // Regex: Matches digits with 1 or 2 decimals (supports . or ,)
                Pattern p = Pattern.compile("(\\d+[.,]\\d{1,2})");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    return "$" + m.group(1).replace(",", ".");
                }
            }
        }
    }

    // 3. FALLBACK STRATEGY: Filtered Maximum
    // If no keyword is found, find the highest number that isn't 'Cash' or 'Change'
    Pattern numPattern = Pattern.compile("\\b\\d+[.,]\\d{1,2}\\b");
    Matcher matcher = numPattern.matcher(cleanText);
    
    double maxValid = 0.0;
    while (matcher.find()) {
        int start = matcher.start();
        String context = cleanText.substring(Math.max(0, start - 20), start);
        
        // Skip numbers associated with payments rather than the cost
        if (context.contains("CASH") || context.contains("CHANGE") || 
            context.contains("TENDERED") || context.contains("RECEIVED")) {
            continue; 
        }

        try {
            double val = Double.parseDouble(matcher.group().replace(",", "."));
            // Filter out clearly wrong numbers (Invoice IDs or massive totals > $5000)
            if (val > maxValid && val < 5000) {
                maxValid = val;
            }
        } catch (Exception e) { /* Skip unparseable matches */ }
    }

    return maxValid > 0 ? "$" + String.format("%.2f", maxValid) : "Not Found";
}
}

