class StatusCodes:
    #FRIENDS
    #ok
    FRIEND_ADDED = {"status": "ok", "code": "FRIEND_ADDED"}
    FRIEND_DELETED = {"status": "ok", "code": "FRIEND_DELETED"}
    INVITE_SENT = {"status": "ok", "code": "INVITE_SENT"}
    #error
    FID_REQUIRED = {"status": "error", "code": "FID_REQUIRED"}
    FID_CANNOT_BE_YOURSELF = {"status": "error", "code": "FID_CANNOT_BE_YOURSELF"}
    FRIENDSHIP_EXISTS = {"status": "error", "code": "FRIENDSHIP_EXISTS"}
    INVITE_ALREADY_SENDED = {"status": "error", "code": "INVITE_ALREADY_SENDED"}
    FUN_REQUIRED = {"status": "error", "code": "FUN_REQUIRED"}
    FUN_CANNOT_BE_YOURSELF = {"status": "error", "code": "FUN_CANNOT_BE_YOURSELF"}
    REQUEST_ALREADY_CANCELED = {"status": "error", "code": "REQUEST_ALREADY_CANCELED"}
    REQUEST_ALREADY_APPROVED = {"status": "error", "code": "REQUEST_ALREADY_APPROVED"}
    REQUEST_ALREADY_SENDED = {"status": "error", "code": "REQUEST_ALREADY_SENDED"}
    RID_REQUIRED = {"status": "error", "code": "RID_REQUIRED"}
    NOT_FOUND = {"status": "error", "code": "NOT_FOUND"}
    
    #REQUESTS
    #ok
    REQUEST_CANCELED = {"status": "ok", "code": "REQUEST_CANCELED"}
    REQUEST_APPROVED = {"status": "ok", "code": "REQUEST_APPROVED"}
    #error
    REQUEST_ALREADY_APPROVED = {"status": "error", "code": "REQUEST_ALREADY_APPROVED"}
    REQUEST_ALREADY_CANCELED = {"status": "error", "code": "REQUEST_ALREADY_CANCELED"}
    REQUEST_ALREADY_SENDED = {"status": "error", "code": "REQUEST_ALREADY_SENDED"}
    RID_REQUIRED = {"status": "error", "code": "RID_REQUIRED"}
    NOT_FOUND = {"status": "error", "code": "NOT_FOUND"}
    REQUEST_NOT_OF_TYPE_ADD_FRIEND = {"status": "error", "code": "REQUEST_NOT_OF_TYPE_ADD_FRIEND"}
    