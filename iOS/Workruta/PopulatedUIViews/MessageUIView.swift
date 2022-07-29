//
//  MessageUIView.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import SwiftUI
import FirebaseDatabase

struct MessageUIView: View {
    
    let this: MessageViewController
    let userEmail: String
    let myId: String
    let userId: String
    let name: String
    let imageUrl: URL
    let access: Bool
    let myName = UserDefaults.standard.string(forKey: "name")!
    let myEmail = UserDefaults.standard.string(forKey: "email")!.safeEmail()
    let database = Database.database().reference()
    let databaseManager = DatabaseManager.shared
    @State var conversationId: String? = nil
    @State var addedNum = 0
    @State var loading = true
    @State var chatDisplayed = false
    @State var added = false
    @State var atBottomOfScroll = false
    @State var scrollToBottom = false
    @State var chatExists = false
    @State var message = ""
    @State var databasePath: String! = nil
    @State var chatDatas: [[String: Any]] = []
    @State var uiImage: UIImage = UIImage()
    private let cacheUtil = CacheUtil()
    
    var body: some View {
        ZStack {
            Colors.mainColor
            VStack(spacing: 0) {
                HStack {
                    VStack{
                        Image(systemName: "arrow.left")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                    }
                    .padding(10)
                    .onTapGesture {
                        this.finish()
                    }
                    Image(uiImage: uiImage)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 50, height: 50)
                        .backgroundImage(imageName: "default_photo")
                        .clipShape(RoundedRectangle(cornerRadius: 25))
                        .contentShape(Circle())
                        .onTapGesture {
                            this.visitProfile()
                        }
                    Text(name)
                        .foregroundColor(Colors.white)
                        .font(.system(size: 18, weight: .bold))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .lineLimit(1)
                        .onTapGesture {
                            this.visitProfile()
                        }
                }
                .padding(5)
                    ScrollView(showsIndicators: false) {
                            ScrollViewReader { proxy in
                                LazyVStack {
                                    ForEach(0..<chatDatas.count, id: \.self){ index in
                                        BubbleUIView(chatData: chatDatas[index])
                                            .onAppear(){
                                                if index == chatDatas.count - 1 {
                                                    atBottomOfScroll = true
                                                    added = false
                                                    addedNum = 0
                                                    if databasePath != nil {
                                                        self.updateRead()
                                                    }
                                                }
                                            }
                                            .onDisappear(){
                                                if index == chatDatas.count - 1 {
                                                    atBottomOfScroll = false
                                                }
                                            }
                                    }
                                }
                                .onAppear(){
                                    proxy.scrollTo(chatDatas.count - 1)
                                }
                                .onChange(of: scrollToBottom, perform: { _ in
                                    withAnimation {
                                        proxy.scrollTo(chatDatas.count - 1)
                                    }
                                })
                                .onChange(of: chatDatas.count) { _ in
                                    if chatDisplayed {
                                        let lastData = chatDatas[chatDatas.count - 1]
                                        let fromMe = lastData["fromMe"] as! Bool
                                        if fromMe || (!fromMe && atBottomOfScroll) {
                                            withAnimation {
                                                proxy.scrollTo(chatDatas.count - 1)
                                            }
                                        } else {
                                            addedNum += 1
                                            added = true
                                        }
                                    } else {
                                        proxy.scrollTo(chatDatas.count - 1)
                                    }
                                    chatDisplayed = true
                                }
                            }
                            .padding(top: 10)
                    }
                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
                    .background(Colors.white)
                    .cornerRadius(30, corners: [.topLeft, .topRight])
                ZStack {
                    VStack{
                        VStack{
                            HStack {
                                TextField("", text: $message){
                                    UIApplication.shared.hideKeyboard(hide: false)
                                }
                                .modifier(PlaceholderStyle(show: message.isEmpty, text: Strings.message))
                                .padding(.leading, 5)
                                .foregroundColor(Colors.black)
                                .font(.system(size: 17))
                                .frame(maxWidth: .infinity)
                                .onTapGesture {
                                    UIApplication.shared.hideKeyboard(hide: false)
                                }
                                Button {
                                    self.sendMessage()
                                } label: {
                                    Image(systemName: "paperplane.fill")
                                        .resizable()
                                        .frame(width: 25, height: 25)
                                        .foregroundColor(Colors.white)
                                        .padding(10)
                                        .background(Colors.mainColor)
                                        .cornerRadius(50)
                                }

                            }
                            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                            .padding(5)
                        }
                        .background(Colors.ash)
                        .cornerRadius(50)
                    }
                    .padding(5)
                }
                .frame(maxWidth: .infinity, maxHeight: 70)
                .background(Colors.white)
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
        .overlay(
            ZStack {
                if added {
                    Text(String(addedNum))
                        .frame(width: 35, height: 35, alignment: .center)
                        .foregroundColor(Colors.white)
                        .font(.system(size: 20, weight: .bold))
                        .background(Colors.normalRed)
                        .cornerRadius(50)
                        .overlay(
                            Circle().stroke(Colors.white, lineWidth: 1.0)
                        )
                        .onTapGesture {
                            scrollToBottom.toggle()
                            added = false
                            addedNum = 0
                        }
                }
            }
                .padding(.bottom, 100)
                .padding(.trailing, 20)
            , alignment: .bottomTrailing
        )
        .onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }
        .onAppear(){
            self.checkChatExists()
        }
    }
    
    func listenForMessages() {
        let chatDB = database.child("\(conversationId!)/messages")
        chatDB.observe(.value) { snapshot in
            guard let allChats = snapshot.value as? [[String: Any]] else {
                return
            }
            var newChat: [[String: Any]] = []
            for chats in allChats {
                let dateStr = chats["date"] as! String
                let date = dateStr.convertToDate()
                let eachChat: [String: Any] = [
                    "fromMe": chats["userFrom"] as! String == self.myId,
                    "textMsg": chats["content"] as! String,
                    "dateStr": date.miniDate(),
                    "sent": true
                ]
                newChat.append(eachChat)
            }
            DispatchQueue.main.async {
                self.chatDatas = newChat
            }
        }
    }
    
    func sendMessage() {
        guard !message.replacingOccurrences(of: " ", with: "").isEmpty else {
            return
        }
        let message = self.message
        let date = Date()
        let dateStr = date.dateTimeStamp()
        self.message = ""
        if chatExists {
            guard let conversationId = conversationId else {
                return
            }
            let miniData: [String: Any] = [
                "fromMe": true,
                "textMsg": message,
                "dateStr": date.miniDate(),
                "sent": false
            ]
            chatDatas.append(miniData)
            let messageDB = database.child("\(conversationId)/messages")
            messageDB.observeSingleEvent(of: .value) { snapshot in
                guard var snapshotContent = snapshot.value as? [[String: Any]] else {
                    return
                }
                let newMessageEntry: [String: Any] = [
                    "id": conversationId,
                    "userFrom": myId,
                    "userTo": userId,
                    "content": message,
                    "date": dateStr,
                    "sender_email": myEmail,
                    "is_read": false,
                    "name": myName
                ]
                snapshotContent.append(newMessageEntry)
                messageDB.setValue(snapshotContent)
                let latestMessage: [String: Any] = [
                    "date": dateStr,
                    "is_read": false,
                    "message": message,
                    "userFrom": myId,
                    "userTo": userId
                ]
                let fromEmailDB = database.child("\(myEmail)/conversations")
                let toEmailDB = database.child("\(userEmail)/conversations")
                fromEmailDB.observeSingleEvent(of: .value) { snapshot in
                    if var conversations = snapshot.value as? [[String: Any]] {
                        var targetConversation: [String: Any]?
                        var position = 0
                        for conversationDictionary in conversations {
                            if let currentId = conversationDictionary["id"] as? String, currentId == conversationId {
                                targetConversation = conversationDictionary
                                break
                            }
                            position += 1
                        }
                        if var targetConversation = targetConversation {
                            targetConversation["latest_message"] = latestMessage
                            conversations[position] = targetConversation
                        } else {
                            let newConversations: [String: Any] = [
                                "id": conversationId,
                                "latest_message": latestMessage,
                                "name": name,
                                "other_user_email": userEmail,
                                "unseen": 0,
                                "userFrom": myId,
                                "userTo": userId
                            ]
                            conversations.append(newConversations)
                        }
                        fromEmailDB.setValue(conversations)
                    }
                }
                toEmailDB.observeSingleEvent(of: .value) { snapshot in
                    if var conversations = snapshot.value as? [[String: Any]] {
                        var targetConversation: [String: Any]?
                        var position = 0
                        for conversationDictionary in conversations {
                            if let currentId = conversationDictionary["id"] as? String, currentId == conversationId {
                                targetConversation = conversationDictionary
                                break
                            }
                            position += 1
                        }
                        if var targetConversation = targetConversation {
                            var unseen = targetConversation["unseen"] as! Int
                            unseen += 1
                            targetConversation["unseen"] = unseen
                            targetConversation["latest_message"] = latestMessage
                            conversations[position] = targetConversation
                        } else {
                            let newConversations: [String: Any] = [
                                "id": conversationId,
                                "latest_message": latestMessage,
                                "name": myName,
                                "other_user_email": myEmail,
                                "unseen": 1,
                                "userFrom": myId,
                                "userTo": userId
                            ]
                            conversations.append(newConversations)
                        }
                        toEmailDB.setValue(conversations)
                    }
                }
            }
        } else {
            createNewMessage(message: message)
        }
    }
    
    func createNewMessage(message: String) {
        chatExists = true
        conversationId = createMessageId()
        let date = Date()
        let dateStr = date.dateTimeStamp()
        let miniData: [String: Any] = [
            "fromMe": true,
            "textMsg": message,
            "dateStr": date.miniDate(),
            "sent": false
        ]
        chatDatas.append(miniData)
        let latestMessage: [String: Any] = [
            "date": dateStr,
            "is_read": false,
            "message": message,
            "userFrom": myId,
            "userTo": userId
        ]
        let conversationsFrom: [String: Any] = [
            "id": conversationId!,
            "latest_message": latestMessage,
            "name": name,
            "other_user_email": userEmail,
            "unseen": 0,
            "userFrom": myId,
            "userTo": userId
        ]
        let conversationsTo: [String: Any] = [
            "id": conversationId!,
            "latest_message": latestMessage,
            "name": myName,
            "other_user_email": myEmail,
            "unseen": 1,
            "userFrom": myId,
            "userTo": userId
        ]
        let fromEmailDB = database.child("\(myEmail)/conversations")
        let toEmailDB = database.child("\(userEmail)/conversations")
        fromEmailDB.observeSingleEvent(of: .value) { snapshot in
            if var conversations = snapshot.value as? [[String: Any]] {
                let index = conversations.count
                self.databasePath = "\(self.myEmail)/conversations/\(String(index))"
                conversations.append(conversationsFrom)
                fromEmailDB.setValue(conversations)
            }
        }
        toEmailDB.observeSingleEvent(of: .value) { snapshot in
            if var conversations = snapshot.value as? [[String: Any]] {
                conversations.append(conversationsTo)
                toEmailDB.setValue(conversations)
            }
        }
        self.listenForMessages()
    }
    
    func checkChatExists(){
        getUserImage()
        let conversationDB = database.child("\(myEmail)/conversations")
        conversationDB.observeSingleEvent(of: .value) { snapshot in
            self.loading = false
            guard let conversations = snapshot.value as? [[String: Any]] else {
                return
            }
            var count = -1
            if let collections = conversations.first(where: {
                guard let otherEmail = $0["other_user_email"] as? String else {
                    return false
                }
                count += 1
                return otherEmail == self.userEmail
            }) {
                guard let id = collections["id"] as? String else {
                    return
                }
                self.chatExists = true
                self.conversationId = id
                self.listenForMessages()
                self.databasePath = "\(self.myEmail)/conversations/\(String(count))"
                self.updateRead()
            }
        }
    }
    
    func updateRead(){
        DispatchQueue.global().async {
            databaseManager.updateRead(databasePath: self.databasePath!)
        }
    }
    
    func createMessageId() -> String {
        let dateLong = Date().dateToLong()
        return "\(myId)_\(userId)_\(String(dateLong))"
    }
    
    func getUserImage(){
        cacheUtil.getImage(imageURL: self.imageUrl) { data, error in
            if let data = data {
                uiImage = UIImage(data: data)!
            }
        }
    }
    
}
