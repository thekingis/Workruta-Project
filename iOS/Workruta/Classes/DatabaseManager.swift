//
//  DatabaseManager.swift
//  Workruta
//
//  Created by The KING on 06/07/2022.
//

import Foundation
import FirebaseDatabase

class DatabaseManager {
    
    public static let shared = DatabaseManager()
    private let database = Database.database().reference()
    
    func updateRead(databasePath: String){
        let unseenDB = database.child("\(databasePath)/unseen")
        let messageDB = database.child("\(databasePath)/latest_message/is_read")
        unseenDB.setValue(0)
        messageDB.setValue(true)
    }
    
}
