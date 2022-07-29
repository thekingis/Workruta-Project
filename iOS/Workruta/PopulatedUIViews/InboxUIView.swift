//
//  InboxUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI
import FirebaseDatabase

struct InboxUIView: View {
    
    let this: InboxViewController
    let myId: String
    let safeEmail: String
    let database = Database.database().reference()
    @State var loading = true
    @State var haveChat = true
    @State var allChats: [[String: Any]] = []
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                HStack {
                    HStack {
                        Image(systemName: "arrow.left")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                        Text("Inbox")
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
                if haveChat {
                    ScrollView(showsIndicators: false) {
                        ScrollViewReader { proxy in
                            LazyVStack {
                                ForEach(0..<allChats.count, id: \.self){ index in
                                    let chats = allChats[index]
                                    MessageListView(this: this, chat: chats)
                                }
                            }
                            .padding(10)
                        }
                    }
                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
                }
                if !haveChat {
                    ZStack{
                        Colors.white
                        Text("Inbox is empty")
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
            self.loadConversations()
        }
    }
    
    func loadConversations(){
        let conversationsDB = database.child("\(safeEmail)/conversations")
        conversationsDB.observe(.value) { snapshot in
            guard let conversations = snapshot.value as? [[String: Any]] else {
                self.loading = false
                self.haveChat = false
                return
            }
            var count = 0
            let chatCount = conversations.count
            var allConversations: [[String: Any]] = []
            self.haveChat = chatCount > count
            for conversation in conversations {
                var newConversation = conversation
                let otherEmail = newConversation["other_user_email"] as! String
                let userDB = self.database.child("\(otherEmail)/photoUrl")
                userDB.observeSingleEvent(of: .value) { snapshot in
                    count += 1
                    if let photoUrl = snapshot.value as? String {
                        newConversation["photoUrl"] = photoUrl.rawUrl()
                    }
                    allConversations.append(newConversation)
                    if count == chatCount {
                        self.loading = false
                        setupConversations(allConversations: allConversations)
                    }
                }
            }
        }
    }
    
    func setupConversations(allConversations: [[String: Any]]) {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        var allChat: [[String: Any]] = []
        var allChatWithKeys: [String: [String: Any]] = [String: [String: Any]]()
        for conversation in allConversations {
            let latestMessage = conversation["latest_message"] as! [String: Any]
            let fromMe = myId == latestMessage["userFrom"] as! String
            let userId = fromMe ? conversation["userTo"] as! String : conversation["userFrom"] as! String
            let userEmail = conversation["other_user_email"] as! String
            let name = conversation["name"] as! String
            let photoUrl = conversation["photoUrl"] as! String
            let dateStr = latestMessage["date"] as! String
            let message = latestMessage["message"] as! String
            let isRead = fromMe || latestMessage["is_read"] as! Bool
            let unseen = conversation["unseen"] as! Int
            let chats: [String: Any] = [
                "fromMe": fromMe,
                "userId": userId,
                "userEmail": userEmail,
                "name": name,
                "photoUrl": photoUrl,
                "dateStr": dateStr,
                "message": message,
                "isRead": isRead,
                "unseen": unseen
            ]
            allChatWithKeys[dateStr] = chats
        }
        
        let sortedArray = allChatWithKeys.map{(formatter.date(from: $0.key)!, [$0.key:$0.value])}
            .sorted{$1.0 < $0.0}
            .map{$1}
        for array in sortedArray {
            allChat.append(array.values.first!)
        }
        self.allChats = allChat
    }
    
}
