package demo;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
class MealRequestSession {

    private static final String MEAL_REQUEST_STATE = "mealRequestState";
    private static final String NO_ACTIVE_MEAL_REQUEST = "There is no active meal request to display.";
    private static final String NO_ACTIVE_MEAL_REQUEST_NOTICE = "no-active-meal-request";

    void store(final HttpServletRequest request, final MealRequestSessionState state) {
        request.getSession()
                .setAttribute(MEAL_REQUEST_STATE, state);
    }

    void clear(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(MEAL_REQUEST_STATE);
        }
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
        return "redirect:/?notice=" + NO_ACTIVE_MEAL_REQUEST_NOTICE;
    }

    boolean isMissingRequestNotice(final String notice) {
        return NO_ACTIVE_MEAL_REQUEST_NOTICE.equals(notice);
    }

    String noActiveRequestMessage() {
        return NO_ACTIVE_MEAL_REQUEST;
    }
}
