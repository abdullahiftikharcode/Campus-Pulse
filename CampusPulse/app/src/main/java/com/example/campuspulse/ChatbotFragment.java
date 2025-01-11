package com.example.campuspulse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ChatbotFragment extends Fragment {

    private RecyclerView chatsRV;
    private EditText userMsgEdt;
    private FloatingActionButton sendMsgFAB;
    private final String BOT_KEY = "bot";
    private final String USER_KEY = "user";
    private ArrayList<ChatsModal> chatsModalArrayList;
    private ChatRVAdapter chatRVAdapter;

    // Replace this with your actual API key
    private static final String API_KEY = "AIzaSyA_VWhvKOBJGZtJmNzeQ-LCCqXSWOgGYN8";
    private static final String GEMINI_URL = "https://generativeai.googleapis.com/v1beta3/generativeModels/gemini-1.5-flash/generate";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        chatsRV = view.findViewById(R.id.idRVChats);
        userMsgEdt = view.findViewById(R.id.idEdtMessage);
        sendMsgFAB = view.findViewById(R.id.idFABSend);

        // Initialize chat list and adapter
        chatsModalArrayList = new ArrayList<>();
        chatRVAdapter = new ChatRVAdapter(chatsModalArrayList, getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        chatsRV.setLayoutManager(manager);
        chatsRV.setAdapter(chatRVAdapter);
        sendInitialContext();
        // Set click listener for the send message button
        sendMsgFAB.setOnClickListener(v -> {
            if (userMsgEdt.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please enter your message", Toast.LENGTH_SHORT).show();
                return;
            }
            getResponse(userMsgEdt.getText().toString());
            userMsgEdt.setText("");
        });

        return view;
    }

    private void getResponse(String message) {
        // Add user message to the list
        chatsModalArrayList.add(new ChatsModal(message, USER_KEY));
        chatRVAdapter.notifyDataSetChanged();

        // Make the request to Gemini API
        new Thread(() -> {
            try {
                String responseText = fetchGeminiResponse(message);
                getActivity().runOnUiThread(() -> {
                    // Add bot response to the chat list
                    chatsModalArrayList.add(new ChatsModal(responseText, BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    // Add error message if request fails
                    chatsModalArrayList.add(new ChatsModal("Error: Unable to get response", BOT_KEY));
                    chatRVAdapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private String fetchGeminiResponse(String message) throws Exception {
        // Use the current Gemini API endpoint
        URL url = new URL("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + API_KEY);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Construct the request body according to the current Gemini API specification
        String jsonBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                message.replace("\"", "\\\"")  // Escape quotes to prevent JSON parsing errors
        );

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read the response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // Parse the JSON response to extract the text
                return parseGeminiResponse(response.toString());
            }
        } else {
            // Handle error response
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    error.append(inputLine);
                }
                throw new Exception("API Error: " + error.toString());
            }
        }
    }
    private void sendInitialContext() {
        // getting context and send it to the AI
        String initialContext = some_context();
        new Thread(() -> {
            try {
                String contextMessage = "Please use the following context to help users with CampusPulse app-related questions: " + initialContext;
                String responseText = fetchGeminiResponse(contextMessage);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    // Helper method to parse the Gemini API response
    private String parseGeminiResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            if (candidates.length() > 0) {
                return candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
            return "No response found";
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
    private String some_context (){
        return ("If the user asks a question in this context you can anaylyze and answer from here else answer normally" +
                "Here are the answers to the frequently asked questions (FAQs) based on the CampusPulse features and the context of your app:\n" +
                "\n" +
                "General Account & Navigation\n" +
                "How do I log in to CampusPulse?\n" +
                "\n" +
                "To log in, open the CampusPulse app and enter your registered username and password. If you don’t have an account, you’ll need to create one first.\n" +
                "How do I change my password on the app?\n" +
                "\n" +
                "To change your password, go to the \"Settings\" section in the app, then select \"Change Password.\" You will be prompted to enter your current password, followed by the new password you'd like to set.\n" +
                "Can I reset my password if I forget it?\n" +
                "\n" +
                "Yes, if you forget your password, you can reset it. Click on the \"Forgot Password?\" link on the login page, and follow the instructions to reset your password via email.\n" +
                "How do I log out of the CampusPulse app?\n" +
                "\n" +
                "To log out, go to the \"Settings\" section and select the \"Log Out\" option. This will end your session and take you back to the login page.\n" +
                "Is my personal data secure on CampusPulse?\n" +
                "\n" +
                "Yes, your personal data is secure on CampusPulse. We use encryption and follow strict security protocols to ensure your data is protected and only accessible by you.\n" +
                "How can I update my profile information (e.g., phone number, email)?\n" +
                "\n" +
                "To update your profile information, go to the \"Profile\" section in the app, then select \"Edit Profile.\" You can update details like your phone number, email, and other personal information here.\n" +
                "Miscellaneous Features\n" +
                "How do I report a technical issue with the app?\n" +
                "\n" +
                "If you encounter any technical issues, go to the \"Help & Support\" section of the app and select \"Report an Issue.\" You can describe the issue and submit it for support.\n" +
                "How can I contact support if I face an issue?\n" +
                "\n" +
                "You can contact support by going to the \"Help & Support\" section in the app and selecting \"Contact Support.\" You can then send an email or chat with a support representative.\n" +
                "Can I use CampusPulse on my desktop or only on my mobile device?\n" +
                "\n" +
                "Currently, CampusPulse is available only on mobile devices (iOS and Android). However, a desktop version may be available in the future.\n" +
                "How do I know if I have new messages or notifications?\n" +
                "\n" +
                "You’ll receive a notification on your mobile device whenever there’s a new message or notification. You can also check the \"Notifications\" section in the app for updates.\n" +
                "How do I create an event for my university club or organization?\n" +
                "\n" +
                "To create an event, go to the \"Events\" section, click \"Create Event,\" and enter the event details such as date, time, location, and description. Once submitted, your event will be visible to all members of your club or organization.\n" +
                "Can I check for upcoming events and activities in my club?\n" +
                "\n" +
                "Yes, you can check upcoming events and activities in the \"Events\" section. This will list all events scheduled by your club or organization.\n" +
                "Event & Club Management\n" +
                "How do I join a club on campus?\n" +
                "\n" +
                "To join a club, go to the \"Clubs\" section in the app, browse through the available clubs, and select the one you’d like to join. Then click \"Join Club\" to become a member.\n" +
                "How can I check upcoming club events?\n" +
                "\n" +
                "To check upcoming club events, go to the \"Clubs\" section, select your club, and view the \"Upcoming Events\" list. You will find all scheduled events here.\n" +
                "Can I communicate with club members directly through the app?\n" +
                "\n" +
                "Yes, you can communicate with club members through the \"Messages\" section of the app. You can send individual or group messages to members of your club.\n" +
                "How do I update my club’s event details?\n" +
                "\n" +
                "To update your club’s event details, go to the \"Events\" section, select the event you want to edit, and click \"Edit Event.\" You can then update the event information such as date, time, or description.\n" +
                "Can I share my event with other students in CampusPulse?\n" +
                "\n" +
                "Yes, you can share your event with other students by selecting the \"Share Event\" option after creating or editing the event. You can share the event through social media, email, or directly within the app.\n");
    }
}
