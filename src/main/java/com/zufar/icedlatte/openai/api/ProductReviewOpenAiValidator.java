package com.zufar.icedlatte.openai.api;

import com.zufar.icedlatte.openai.exception.InappropriateContentException;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class ProductReviewOpenAiValidator {

    private final OpenAiChatModel chatModel;

    @Autowired
    public ProductReviewOpenAiValidator(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map checkProductReviewContent(@RequestParam("review") String review) {

        String promptString = """
            |You are an assistant who checks texts for appropriateness.
            |Analyze the following text {review} and determine if it contains political statements,
            |non-English language, or inappropriate language.
            |If you find any of these issues, return the phrase 'not appropriate', after return the reason in curly brackets. 
            |Only the reason should be in curly brackets.
            |Else return OK
            """;
        PromptTemplate promptTemplate = new PromptTemplate(promptString);
        promptTemplate.add("review", review);

        ChatResponse response = chatModel.call(new Prompt(List.of(promptTemplate.createMessage()),
                OpenAiChatOptions.builder().build()));

        List<Generation> results = response.getResults();
        Map<String, String> resultMap = new HashMap();
        for (Generation result: results) {
            String content = result.getOutput().getContent();
            if (content.startsWith("not appropriate")) {
                String reason = extractReason(content);
                throw new InappropriateContentException(reason);
            } else {
                resultMap.put("generation", content);
            }
        }
        return resultMap;
    }

    private String extractReason(String content) {
        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Unknown reason";
    }
}
