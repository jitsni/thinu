/**
 * Copyright 2017 Jitendra Kotamraju.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitu.thinu;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.OutputSpeech;

public class ThinuSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(ThinuSpeechlet.class);

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        log.info("onIntent requestId={}, sessionId={}, slots={}",
                request.getRequestId(),
                requestEnvelope.getSession().getSessionId(),
                request.getIntent().getSlots());

        Intent intent = request.getIntent();
        String intentName = intent.getName();

        if ("ThinuIntent".equals(intentName)) {
            String person = null;
            String meal = null;
            String food = null;
            String place = null;
            String time = null;

            Slot personSlot = intent.getSlot("person");
            if (personSlot != null) {
                person = personSlot.getValue();
            }

            Slot mealSlot = intent.getSlot("meal");
            if (mealSlot != null) {
                meal = mealSlot.getValue();
            }

            Slot foodSlot = intent.getSlot("food");
            if (foodSlot != null) {
                food = foodSlot.getValue();
            }

            Slot placeSlot = intent.getSlot("place");
            if (placeSlot != null) {
                place = placeSlot.getValue();
            }

            Slot timeSlot = intent.getSlot("time");
            if (timeSlot != null) {
                time = timeSlot.getValue();
            }

            log.info("onIntent person={} meal={} food={} place={} time={}", person, meal, food, place, time);

            return getThinuResponse(requestEnvelope.getSession(), person, meal, food, place, time);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            return getStopResponse();
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            return getCancelResponse();
        } else {
            return getAskResponse("Ok", "This is unsupported.  Please try something else.");
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "What did you eat ?";
        return getAskResponse("Ok", speechText);
    }

    /**
     * Creates a {@code SpeechletResponse} for the thinu intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getThinuResponse(Session session, String person, String meal, String food, String place, String time) {
        if (person == null) {
            person = (String) session.getAttribute("person");
            if (person == null) {
                person = session.getUser().getUserId();
            }
        }
        if (person != null) {
            session.setAttribute("person", person);
        }

        if (meal == null) {
            meal = (String) session.getAttribute("meal");
        } else {
            session.setAttribute("meal", meal);
        }

        if (food == null) {
            food = (String) session.getAttribute("food");
        } else {
            session.setAttribute("food", food);
        }

        if (place == null) {
            place = (String) session.getAttribute("place");
        } else {
            session.setAttribute("place", place);
        }

        if (time == null) {
            time = (String) session.getAttribute("time");
        } else {
            session.setAttribute("time", time);
        }

        if (person == null) {
            return getPersonResponse();
        } else if (meal == null) {
            return getMealResponse();
        } else if (place == null) {
            return getPlaceResponse();
        } else {
            log.info("onIntent person={} meal={} food={} place={} time={}", person, meal, food, place, time);
            return getFinalResponse(meal);
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say what you have eaten!";
        return getAskResponse("Ok", speechText);
    }

    private SpeechletResponse getStopResponse() {
        String speechText = "Goodbye";
        return getTellResponse(speechText);
    }

    private SpeechletResponse getCancelResponse() {
        String speechText = "Goodbye";
        return getTellResponse(speechText);
    }

    private SpeechletResponse getPersonResponse() {
        String speechText = "Who are you ?";
        return getAskResponse("person", speechText);
    }

    private SpeechletResponse getMealResponse() {
        String speechText = "Is it breakfast, or lunch or dinner ?";
        return getAskResponse("meal", speechText);
    }

    private SpeechletResponse getPlaceResponse() {
        String speechText = "Where did you eat ? home or restaurant";
        return getAskResponse("place", speechText);
    }

    private SpeechletResponse getFinalResponse(String meal) {
        String speechText = "Ok. I hope you enjoyed your " + meal;
        return getTellResponse(speechText);
    }

    /**
     * Helper method that creates a card object.
     * @param title title of the card
     * @param content body of the card
     * @return SimpleCard the display card to be sent along with the voice response.
     */
    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    /**
     * Helper method for retrieving an OutputSpeech object when given a string of TTS.
     * @param speechText the text that should be spoken out to the user.
     * @return an instance of SpeechOutput.
     */
    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    /**
     * Helper method that returns a reprompt object. This is used in Ask responses where you want
     * the user to be able to respond to your speech.
     * @param outputSpeech The OutputSpeech object that will be said once and repeated if necessary.
     * @return Reprompt instance.
     */
    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    /**
     * Helper method for retrieving an Ask response with a simple card and reprompt included.
     * @param cardTitle Title of the card that you want displayed.
     * @param speechText speech text that will be spoken to the user.
     * @return the resulting card and speech text.
     */
    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private SpeechletResponse getTellResponse(String speechText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(speechText);

        return SpeechletResponse.newTellResponse(outputSpeech);
    }
}
