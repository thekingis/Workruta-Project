//
//  MessageListView.swift
//  Workruta
//
//  Created by The KING on 05/07/2022.
//

import SwiftUI

struct MessageListView: View {
    
    let this: InboxViewController
    let chat: [String: Any]
    @State var uiImage = UIImage()
    private let cacheUtil = CacheUtil()
    
    var body: some View {
        let fromMe = chat["fromMe"] as! Bool
        let userId = chat["userId"] as! String
        let userEmail = chat["userEmail"] as! String
        let name = chat["name"] as! String
        let photoUrl = chat["photoUrl"] as! String
        let dateStr = chat["dateStr"] as! String
        let message = chat["message"] as! String
        let isRead = chat["isRead"] as! Bool
        let unseen = chat["unseen"] as! Int
        let photoURL = URL(string: Constants.www + photoUrl)!
        let date = dateStr.convertToDate()
        let dateSTR = date.minify()
        
        Button {
            this.openMessenger(userId: userId, name: name, userEmail: userEmail, photoUrl: photoURL)
        } label: {
            HStack(alignment: .center, spacing: 5) {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 50, height: 50)
                    .backgroundImage(imageName: "default_photo")
                    .clipShape(RoundedRectangle(cornerRadius: 25))
                    .contentShape(Circle())
                    .overlay(RoundedRectangle(cornerRadius: 25).stroke(Colors.mainColor, lineWidth: 2.0))
                VStack(alignment: .center, spacing: 5){
                    HStack(alignment: .center, spacing: 5) {
                        Text(name)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .foregroundColor(Colors.black)
                            .font(.system(size: 16, weight: .bold))
                            .lineLimit(1)
                        Text(dateSTR)
                            .frame(width: 80, alignment: .trailing)
                            .foregroundColor(Colors.asher)
                            .font(.system(size: 12))
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    HStack(alignment: .center, spacing: 5)  {
                        Image(systemName: fromMe ? "square.and.arrow.up" : "square.and.arrow.down")
                            .resizable()
                            .frame(width: 15, height: 17)
                            .foregroundColor(fromMe ? Colors.asher : Colors.mainColor)
                        Text(message)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .foregroundColor(Colors.black)
                            .font(.system(size: 14, weight: isRead ? .regular : .bold))
                            .lineLimit(1)
                        if !fromMe && unseen > 0 {
                            Text(String(unseen))
                                .frame(width: 20, height: 20, alignment: .center)
                                .foregroundColor(Colors.white)
                                .font(.system(size: 12))
                                .background(Colors.mainColor)
                                .cornerRadius(50)
                        }
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .center)
            .background(Colors.white)
            .padding(10)
            .border(Colors.asher, width: 1.0)
            .cornerRadius(7)
            .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
        }
        .onAppear(){
            getUserImage(imageUrl: photoURL)
        }
    }
    
    func getUserImage(imageUrl: URL){
        cacheUtil.getImage(imageURL: imageUrl) { data, error in
            if let data = data {
                uiImage = UIImage(data: data)!
            }
        }
    }
    
}
