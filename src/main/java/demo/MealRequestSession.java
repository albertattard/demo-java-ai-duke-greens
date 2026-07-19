package demo;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import module java.base;

@Component
class MealRequestSession {

    private static final String MEAL_REQUEST_STATE = "mealRequestState";
    private static final String MEAL_CONVERSATION_ID = "mealConversationId";
    private static final String SIMULATED_ORDER_COMPLETION = "simulatedOrderCompletion";
    private static final String FEEDBACK_SUBMISSION = "feedbackSubmission";
    private static final String FEEDBACK_CONFIRMATION = "feedbackConfirmation";
    private static final String NO_ACTIVE_MEAL_REQUEST = "There is no active meal request to display.";
    private static final String NO_ACTIVE_MEAL_REQUEST_NOTICE = "no-active-meal-request";
    private static final String BASKET_UNAVAILABLE_NOTICE = "basket-unavailable";
    private static final String BASKET_UNAVAILABLE = "This basket is no longer available.";

    void store(final HttpServletRequest request, final MealRequestSessionState state) {
        request.getSession()
                .setAttribute(MEAL_REQUEST_STATE, state);
    }

    void clear(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(MEAL_REQUEST_STATE);
            session.removeAttribute(MEAL_CONVERSATION_ID);
        }
    }

    String startConversation(final HttpServletRequest request) {
        final String conversationId = UUID.randomUUID().toString();
        request.getSession().setAttribute(MEAL_CONVERSATION_ID, conversationId);
        return conversationId;
    }

    String conversationId(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        return session != null
                && session.getAttribute(MEAL_CONVERSATION_ID) instanceof final String conversationId
                ? conversationId
                : null;
    }

    boolean hasConversation(final HttpServletRequest request, final String conversationId) {
        return conversationId != null
                && conversationId.equals(conversationId(request));
    }

    String recommendationsRedirect(final HttpServletRequest request) {
        final String conversationId = conversationId(request);
        return conversationId == null
                ? initialRequestRedirect()
                : recommendationsRedirect(conversationId);
    }

    String recommendationsRedirect(final String conversationId) {
        return "redirect:/demo/recommendations/" + conversationId;
    }

    void markSimulatedOrderCompleted(final HttpServletRequest request) {
        request.getSession().setAttribute(SIMULATED_ORDER_COMPLETION, true);
    }

    boolean consumeSimulatedOrderCompletion(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null
                || !Boolean.TRUE.equals(session.getAttribute(SIMULATED_ORDER_COMPLETION))) {
            return false;
        }

        session.removeAttribute(SIMULATED_ORDER_COMPLETION);
        session.setAttribute(FEEDBACK_SUBMISSION, true);
        return true;
    }

    boolean claimFeedbackSubmission(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        synchronized (session) {
            if (!Boolean.TRUE.equals(session.getAttribute(FEEDBACK_SUBMISSION))) {
                return false;
            }

            session.removeAttribute(FEEDBACK_SUBMISSION);
            return true;
        }
    }

    void restoreFeedbackSubmission(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                session.setAttribute(FEEDBACK_SUBMISSION, true);
            }
        }
    }

    void completeFeedbackSubmission(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(FEEDBACK_SUBMISSION);
            session.setAttribute(FEEDBACK_CONFIRMATION, true);
        }
    }

    boolean consumeFeedbackConfirmation(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute(FEEDBACK_CONFIRMATION))) {
            return false;
        }

        session.removeAttribute(FEEDBACK_CONFIRMATION);
        return true;
    }

    MealRequestSessionState state(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        final Object state = session.getAttribute(MEAL_REQUEST_STATE);
        return state instanceof final MealRequestSessionState mealRequestSessionState
                ? mealRequestSessionState
                : null;
    }

    String initialRequestRedirect() {
        return "redirect:/demo?notice=" + NO_ACTIVE_MEAL_REQUEST_NOTICE;
    }

    String unavailableBasketRedirect(final HttpServletRequest request) {
        final String active = conversationId(request);
        return (active == null ? "redirect:/demo" : recommendationsRedirect(active))
                + "?notice=" + BASKET_UNAVAILABLE_NOTICE;
    }

    boolean isMissingRequestNotice(final String notice) {
        return NO_ACTIVE_MEAL_REQUEST_NOTICE.equals(notice)
                || BASKET_UNAVAILABLE_NOTICE.equals(notice);
    }

    String noActiveRequestMessage() {
        return NO_ACTIVE_MEAL_REQUEST;
    }

    boolean isBasketUnavailableNotice(final String notice) {
        return BASKET_UNAVAILABLE_NOTICE.equals(notice);
    }

    String basketUnavailableMessage() {
        return BASKET_UNAVAILABLE;
    }
}
