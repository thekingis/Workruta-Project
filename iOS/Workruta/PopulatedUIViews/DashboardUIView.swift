//
//  DashboardUIView.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import SwiftUI
import FirebaseDatabase

struct DashboardUIView: View {
    
    let this: DashboardViewController
    @ObservedObject var models: Models
    let name: String
    let imageUrl: URL
    let database = Database.database().reference()
    let myId = UserDefaults.standard.string(forKey: "myId")!
    let safeEmail = UserDefaults.standard.string(forKey: "email")!.safeEmail()
    @State var unseenMsg: Int = 0
    @State var unseenNote: Int = 0
    @State var menuWidth: CGFloat = 0
    @State var showUnseenMsgs = false
    @State var showUnseenNotes = false
    @State var blackIsShowing = false
    @State var logIsShowing = false
    @State var uiImage: UIImage = UIImage()
    private let cacheUtil = CacheUtil()
    
    var body: some View {
        ZStack {
            Colors.white
            ScrollView(showsIndicators: false) {
                VStack{
                    Spacer().frame(height: 40)
                    ForEach(0...4, id: \.self){ vert in
                        HStack{
                            Spacer()
                            ForEach(0...1, id: \.self){ horiz in
                                Button {
                                    let rowM = ((vert + 1) * 2) - 1
                                    let index = rowM + horiz + 3
                                    listenToItemClick(index: index, hideMenu: true)
                                } label: {
                                    let itemImg = Boxes.images[vert][horiz]
                                    let itemTxt = Boxes.texts[vert][horiz]
                                    VStack{
                                        Spacer()
                                        Image(itemImg).resizable().frame(width: (UIScreen.main.bounds.width / 2) - 100, height: (UIScreen.main.bounds.width / 2) - 100)
                                        Spacer()
                                        Text(itemTxt).foregroundColor(Colors.mainColor)
                                    }.frame(width: (UIScreen.main.bounds.width / 2) - 30, height: (UIScreen.main.bounds.width / 2) - 30).padding(10).cornerRadius(10).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.asher, lineWidth: 1.0))
                                }
                                Spacer()
                            }
                        }.padding(top: 10, bottom: 10)
                    }
                    Spacer().frame(height: 40)
                }
            }.padding(left: 0, top: 60, right: 0, bottom: 40).onTapGesture {
                menuWidth = 0
            }
        }.background(Colors.white)
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading).overlay(
                HStack{
                    Image(systemName: "location.fill").resizable().frame(width: 20, height: 20).foregroundColor(models.locIcnClr).padding(left: 10)
                    Text(models.userAddress)
                        .foregroundColor(models.locTxtClr).font(.system(size: 16)).padding(left: 13)
                    Spacer()
                }.background(Colors.white).frame(height: 40).border(width: 1, edges: [.top], color: Colors.mainColor)
                , alignment: .bottomLeading
            ).overlay(
                VStack{
                    HStack(alignment: .center, spacing: 10){
                        Image(uiImage: uiImage).resizable().scaledToFill().frame(width: 50, height: 50).backgroundImage(imageName: "default_photo").clipShape(RoundedRectangle(cornerRadius: 25)).contentShape(Circle()).onTapGesture {
                            if menuWidth == 0 {
                                menuWidth = UIScreen.main.bounds.width - 100
                            } else {
                                menuWidth = 0
                            }
                        }
                        Text(name).foregroundColor(Colors.white).font(.system(size: 18, weight: .bold)).frame(maxWidth: UIScreen.main.bounds.width - 170, alignment: .leading).onTapGesture {
                            if menuWidth == 0 {
                                menuWidth = UIScreen.main.bounds.width - 100
                            } else {
                                menuWidth = 0
                            }
                        }
                        ZStack {
                            Image(systemName: "envelope.fill")
                                .resizable()
                                .foregroundColor(Colors.white)
                                .frame(width: 30, height: 20)
                                .rotationEffect(.degrees(-35))
                            if showUnseenMsgs {
                                VStack{
                                    Text(String(unseenMsg))
                                        .frame(width: 24, height: 24, alignment: .center)
                                        .foregroundColor(Colors.white)
                                        .font(.system(size: 15, weight: .bold))
                                        .background(Colors.lightRed)
                                        .cornerRadius(50)
                                }
                                .padding(EdgeInsets(top: 0, leading: 0, bottom: -18, trailing: -14))
                            }
                        }.onTapGesture {
                            menuWidth = 0
                            this.openViewController(index: 1)
                        }
                        ZStack {
                            Image(systemName: "bell.fill")
                                .resizable()
                                .foregroundColor(Colors.white)
                                .frame(width: 20, height: 25)
                                .padding(sides: [.left, .right], value: 10)
                                .rotationEffect(.degrees(-35))
                            if showUnseenNotes {
                                VStack{
                                    Text(String(unseenNote))
                                        .frame(width: 24, height: 24, alignment: .center)
                                        .foregroundColor(Colors.white)
                                        .font(.system(size: 15, weight: .bold))
                                        .background(Colors.lightRed)
                                        .cornerRadius(50)
                                }
                                .padding(EdgeInsets(top: 0, leading: 0, bottom: -18, trailing: -14))
                            }
                        }.onTapGesture {
                            menuWidth = 0
                            this.openViewController(index: 2)
                        }
                    }.padding(10).frame(width: UIScreen.main.bounds.width, height: 60)
                }.background(Colors.mainColor)
                , alignment: .topLeading
            ).overlay(
                ScrollView(showsIndicators: false){
                    VStack{
                        Button {
                            listenToItemClick(index: 0, hideMenu: false)
                        } label: {
                            VStack{
                                HStack(alignment: .center, spacing: 10){
                                    Text("")
                                    Image(uiImage: uiImage).resizable().scaledToFill().frame(width: 45, height: 45).backgroundImage(imageName: "default_photo").clipShape(RoundedRectangle(cornerRadius: 22.5)).contentShape(Circle())
                                    Text(name).foregroundColor(Colors.black).font(.system(size: 17))
                                    Spacer()
                                }.frame(width: menuWidth).padding(top: 15, bottom: 15).animation(.easeInOut(duration: 0.3), value: menuWidth)
                            }.background(Colors.ash).border(width: 1, edges: [.top, .trailing, .bottom], color: Colors.mainColor)
                        }
                        ForEach(1...13, id: \.self){ itemIndex in
                            Button {
                                listenToItemClick(index: itemIndex, hideMenu: false)
                            } label: {
                                let index = itemIndex - 1
                                VStack{
                                    HStack(alignment: .center, spacing: 10){
                                        let itemImg = Items.images[index][0]
                                        let itemTxt = Items.texts[index][0]
                                        Text("")
                                        Image(itemImg).resizable().frame(width: 30, height: 30)
                                        Text(itemTxt).foregroundColor(Colors.black).font(.system(size: 17))
                                        Spacer()
                                    }.frame(width: menuWidth).padding(top: 15, bottom: 15).animation(.easeInOut(duration: 0.3), value: menuWidth)
                                }.background(Colors.ash).border(width: 1, edges: [.top, .trailing, .bottom], color: Colors.mainColor)
                            }

                        }
                    }
                }.frame(width: menuWidth, height: UIScreen.main.bounds.height - 120).border(width: 2, edges: [.trailing], color: Colors.mainColor).background(Colors.ash).padding(top: 20).animation(.easeInOut(duration: 0.3), value: menuWidth)
                , alignment: .leading
            ).overlay(
                ZStack{
                    if blackIsShowing {
                        Colors.blackFade
                        VStack (spacing: 30){
                            Text(Strings.logout_text).multilineTextAlignment(.center).font(.system(size: 16)).foregroundColor(Colors.black)
                            HStack (spacing: 15){
                                Spacer()
                                Button {
                                    blackIsShowing = false
                                } label: {
                                    Text(Strings.cancel).foregroundColor(Colors.white).padding(20).background(Colors.black).cornerRadius(5)
                                }
                                Button {
                                    logoutUser()
                                } label: {
                                    Text(Strings.logout).foregroundColor(Colors.white).padding(20).background(Colors.mainColor).cornerRadius(5)
                                }
                            }
                        }.frame(width: UIScreen.main.bounds.width - 60).padding(20).background(Colors.white).cornerRadius(10)
                    }
                }
                , alignment: .topLeading
            ).overlay(
                ZStack{
                    if logIsShowing {
                        Colors.blackFade
                        HStack (alignment: .center, spacing: 10){
                            GIFView(gifName: "loader").frame(width: 20, height: 20, alignment: .center)
                            Text(Strings.logging_out).font(.system(size: 16)).foregroundColor(Colors.asher)
                            Spacer()
                        }.frame(width: UIScreen.main.bounds.width - 60).padding(left: 30, top: 50, bottom: 50).background(Colors.white).cornerRadius(10)
                    }
                }
                , alignment: .topLeading
            ).onAppear(){
                self.getUserImage()
                self.listenForUpdates()
            }
    }
    
    func listenForUpdates(){
        let conversationsDB = database.child("\(safeEmail)/conversations")
        let notificationsDB = database.child("notifications/\(myId)")
        conversationsDB.observe(.value) { snapshot in
            guard let conversations = snapshot.value as? [[String: Any]] else {
                return
            }
            self.unseenMsg = 0
            for conversation in conversations {
                let unseen = conversation["unseen"] as! Int
                self.unseenMsg += unseen
            }
            self.showUnseenMsgs = self.unseenMsg > 0
        }
        notificationsDB.observe(.value) { snapshot in
            guard let notifications = snapshot.value as? [String: [String: Any]] else {
                return
            }
            self.unseenNote = 0
            for (_, value) in notifications {
                let unseen = value["unseen"] as! Int
                self.unseenNote += unseen
            }
            self.showUnseenNotes = self.unseenNote > 0
        }
    }
    
    func listenToItemClick(index: Int, hideMenu: Bool){
        let menuIsHidden = menuWidth == 0
        menuWidth = 0
        if !menuIsHidden && hideMenu {
            return
        }
        switch index {
            case 3:
                return
            case 13:
                blackIsShowing = true
            default:
                this.openViewController(index: index)
        }
    }
    
    func logoutUser() {
        blackIsShowing = false
        logIsShowing = true
        this.logoutUser()
    }
    
    func getUserImage(){
        cacheUtil.getImage(imageURL: imageUrl) { data, error in
            if let data = data {
                uiImage = UIImage(data: data)!
            }
        }
    }
    
}
