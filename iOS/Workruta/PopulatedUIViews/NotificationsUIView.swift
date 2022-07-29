//
//  NotificationsUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI
import FirebaseDatabase

struct NotificationsUIView: View {
    
    let this: NotificationsViewController
    let database = Database.database().reference()
    let myId = UserDefaults.standard.string(forKey: "myId")!
    @State var loading = true
    @State var haveNotes = true
    @State var allNotes: [[String: Any]] = []
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                HStack {
                    HStack {
                        Image(systemName: "arrow.left")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                        Text("Notifications")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                    }
                    .padding(10)
                    .onTapGesture {
                        this.finish()
                    }
                    Spacer()
                }
                .background(Colors.mainColor)
                if haveNotes {
                    ScrollView(showsIndicators: false) {
                        ScrollViewReader { proxy in
                            LazyVStack {
                                ForEach(0..<allNotes.count, id: \.self){ index in
                                    let chats = allNotes[index]
                                    NotificationLists(this: this, that: self, notes: chats)
                                }
                            }
                            .padding(10)
                        }
                    }
                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
                }
                if !haveNotes {
                    ZStack{
                        Colors.white
                        Text("Notification is empty")
                            .foregroundColor(Colors.black)
                            .font(.system(size: 17, weight: .bold))
                    }
                }
            }
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
            if loading {
                ZStack{
                    Colors.whiteFade
                    GIFView(gifName: "loader")
                        .frame(width: 30, height: 30, alignment: .center)
                }
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .onAppear(){
            self.loadNotifications()
        }
    }
    
    func loadNotifications(){
        let notificationsDB = database.child("notifications/\(myId)")
        notificationsDB.observe(.value) { snapshot in
            self.loading = false
            guard let notifications = snapshot.value as? [String :[String: Any]] else {
                self.haveNotes = false
                return
            }
            let noteCount = notifications.count
            var allNotifications: [[String: Any]] = []
            self.haveNotes = noteCount > 0
            for (key, value) in notifications {
                var notification = value
                notification["key"] = key
                allNotifications.append(notification)
            }
            setupNotifications(allNotifications: allNotifications)
        }
    }
    
    func setupNotifications(allNotifications: [[String: Any]]) {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        var allNote: [[String: Any]] = []
        var allNoteWithKeys: [String: [String: Any]] = [String: [String: Any]]()
        for notification in allNotifications {
            let key = notification["key"] as! String
            let dataId = notification["dataId"] as! String
            let dataType = notification["dataType"] as! String
            let date = notification["date"] as! String
            let extraId = notification["extraId"] as! String
            let unseen = notification["unseen"] as! Int
            let userFrom = notification["userFrom"] as! String
            let isSeen = unseen == 0
            let notes: [String: Any] = [
                "unseen": unseen,
                "key": key,
                "extraId": extraId,
                "date": date,
                "dataType": dataType,
                "userFrom": userFrom,
                "isSeen": isSeen,
                "dataId": dataId
            ]
            allNoteWithKeys[date] = notes
        }
        
        let sortedArray = allNoteWithKeys.map{(formatter.date(from: $0.key)!, [$0.key:$0.value])}
            .sorted{$1.0 < $0.0}
            .map{$1}
        for array in sortedArray {
            allNote.append(array.values.first!)
        }
        self.allNotes = allNote
    }
    
    func sendAction(action: String, userTo: String, key: String, dataId: String, extraId: String) {
        guard let url = URL(string: Constants.actionsUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "action": "followAction",
            "todo": action,
            "userTo": userTo,
            "key": key,
            "dataId": dataId,
            "extraId": extraId
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { _, _, _ in }
        urlSession.resume()
    }
}
