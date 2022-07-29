//
//  BubbleUIView.swift
//  Workruta
//
//  Created by The KING on 30/06/2022.
//

import SwiftUI

struct BubbleUIView: View {
    
    let chatData: [String: Any]
    @State var showTime = false
    
    var body: some View {
        
        let fromMe = chatData["fromMe"] as! Bool
        let sent = chatData["sent"] as! Bool
        let textMsg = chatData["textMsg"] as! String
        let dateStr = chatData["dateStr"] as! String
        
        VStack(alignment: fromMe ? .trailing : .leading) {
            HStack{
                Text(textMsg)
                    .padding(15)
                    .background(fromMe ? Colors.mainColor : Colors.ash)
                    .foregroundColor(fromMe ? Colors.white : Colors.black)
                    .cornerRadius(20)
            }
            .frame(maxWidth: 300, alignment: fromMe ? .trailing : .leading)
            .opacity(sent ? 1 : 0.5)
            .onTapGesture {
                showTime.toggle()
                UIApplication.shared.hideKeyboard(hide: true)
            }
            if showTime {
                Text(dateStr)
                    .font(.caption2)
                    .foregroundColor(Colors.asher)
                    .padding(fromMe ? .trailing : .leading, 5)
            }
        }
        .frame(maxWidth: .infinity, alignment: fromMe ? .trailing : .leading)
        .padding(.horizontal, 10)
        .onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }
    }
}
