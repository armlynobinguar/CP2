public class OperationResult<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private OperationResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    static <T> OperationResult<T> ok(T data) {
        return new OperationResult<T>(true, "OK", data);
    }

    static <T> OperationResult<T> fail(String message) {
        return new OperationResult<T>(false, message, null);
    }

    boolean isSuccess() {
        return success;
    }

    String getMessage() {
        return message;
    }

    T getData() {
        return data;
    }
}
