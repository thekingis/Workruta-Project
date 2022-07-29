//
//  ProfileUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI

struct ProfileUIView: View {
    
    let this: ProfileViewController
    let myId: String
    let userId: String
    let access: Bool
    @State var dataObject = [String: Any]()
    @State var imageUrl = URL(string: "")
    @State var name = "User Profile"
    @State var userEmail = ""
    @State var uiImage = UIImage()
    @State var requesting = true
    let cacheUtil = CacheUtil()
    let keys: [String] = ["data", "licenceDetail", "carDetail", "bankDetail"]
    
    var body: some View {
        ZStack {
            Colors.white
            ScrollView{
                VStack {
                    HStack {
                        Spacer()
                        ZStack(alignment: .bottomTrailing) {
                            VStack(){
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .scaledToFill()
                                    .clipShape(RoundedRectangle(cornerRadius: 150))
                            }.frame(width: 200, height: 200, alignment: .center).cornerRadius(100).overlay(RoundedRectangle(cornerRadius: 100.0).stroke(Colors.mainColor, lineWidth: 3.0)).backgroundImage(imageName: "default_photo", cornerRadius: 100).contentShape(Circle())
                            if access {
                                VStack{
                                    ZStack{
                                        Image(systemName: "camera.fill")
                                            .resizable()
                                            .frame(width: 20, height: 17)
                                            .foregroundColor(Colors.white)
                                    }
                                    .frame(width: 40, height: 40)
                                    .background(Colors.mainColor)
                                    .cornerRadius(20)
                                    .contentShape(Circle())
                                    .onTapGesture {
                                        this.changePhoto()
                                    }
                                }
                                .padding(right: 10, bottom: 10)
                            }
                        }.frame(width: 200, height: 200, alignment: .bottomTrailing)
                        Spacer()
                    }
                    VStack{
                        Text(name)
                            .foregroundColor(Colors.black)
                            .font(.system(size: 22, weight: .bold))
                        if !access {
                            Button {
                                this.openMessenger(name: name, userEmail: userEmail, photoUrl: imageUrl!)
                            } label: {
                                Text(Strings.send_message)
                                    .frame(width: (UIScreen.main.bounds.width - 120) / 2)
                                    .foregroundColor(Colors.asher)
                                    .padding(10)
                                    .border(Colors.asher, width: 1.0)
                                    .cornerRadius(7)
                                    .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.ash, lineWidth: 1.0))
                            }
                        }
                    }
                    if dataObject.count > 0 {
                        ForEach(0..<keys.count, id: \.self){ index in
                            let objectKey = keys[index]
                            if dataObject.containsKey(objectKey){
                                let object = dataObject[objectKey] as! [String: Any]
                                ProfileDataUIView(this: this, headerKey: objectKey, dataObject: object, access: access)
                            }
                        }
                    }
                }
                .padding(top: 20, bottom: 30)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(top: 40)
            if requesting {
                ZStack{
                    Colors.whiteFade
                    GIFView(gifName: "loader")
                        .frame(width: 30, height: 30, alignment: .center)
                }
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .overlay(
            HStack(alignment: .center, spacing: 10){
                (Text(Image(systemName: "chevron.left")) + Text(name)).foregroundColor(Colors.white).padding(10).font(.system(size: 18)).onTapGesture {
                    this.finish()
                }
                Spacer()
            }
                .background(Colors.mainColor)
            , alignment: .topLeading
        )
        .onAppear(){
            getData()
        }
    }
    
    func getData() {
        guard let url = URL(string: Constants.profileUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "myId": myId,
            "userId": userId
        ]
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let data = object["data"] as! [String: String]
                            let photo = data["photo"]
                            self.imageUrl = URL(string: Constants.www + photo!)!
                            self.userEmail = data["email"]!
                            self.name = data["name"]!
                            self.dataObject = object as! [String : Any]
                            self.getUserImage()
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
                self.requesting = false
            }
        }
        urlSession.resume()
    }
    
    func getUserImage(){
        cacheUtil.getImage(imageURL: self.imageUrl!) { data, error in
            if let data = data {
                uiImage = UIImage(data: data)!
            }
        }
    }
    
}
