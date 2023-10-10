package rys.nats.exception

class InternalException : RuntimeException {
    constructor() : super("Parsing error. See logs for further details.")
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
