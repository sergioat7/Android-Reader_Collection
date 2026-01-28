//
//  UserResponseIos.swift
//  ReaderCollection
//
//  Created by Sergio Aragonés on 23/1/26.
//

import App

struct UserResponseIos: Decodable {
    
    let id: String
    let username: String
    let status: String
    
    init(from user: UserResponse) {
        self.id = user.id
        self.username = user.username
        self.status = user.status.name
    }
    
    enum CodingKeys: String, CodingKey {
        case id, username, status
    }
}

extension UserResponseIos {
    func toUserResponse() -> UserResponse {
        let status = switch self.status {
        case "PENDING_MINE": RequestStatus.pendingMine
        case "PENDING_FRIEND": RequestStatus.pendingFriend
        case "APPROVED": RequestStatus.approved
        case "REJECTED": RequestStatus.rejected
        default: RequestStatus.pendingMine
        }
        return UserResponse(
            id: self.id,
            username: self.username,
            status: status
        )
    }
}
