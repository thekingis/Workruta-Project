//
//  PaymentsUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI

struct PaymentsUIView: View {
    
    let this: PaymentsViewController
    let myId = UserDefaults.standard.string(forKey: "myId")!
    let keys: [String] = ["idCard", "licenceDetail", "carDetail", "bankDetail"]
    let states: [String: [Any]] = [
        "verified": ["ID Verified", Colors.green],
        "attached": ["Information Attached", Colors.green],
        "pending": ["Pending Verification", Colors.yellow],
        "expired": ["ID Expired", Colors.normalRed],
        "none": ["Not Attached", Colors.normalRed],
        "invalid": ["Invalid ID", Colors.normalRed]
    ]
    let keyTitles: [String: String] = [
        "idCard": "Identity Card",
        "licenceDetail": "Driver's Licence",
        "carDetail": "Car Details",
        "bankDetail": "Bank Details"
    ]
    let keyBGImages: [String: String] = [
        "idCard": "details_idcard",
        "licenceDetail": "details_licence",
        "carDetail": "details_car",
        "bankDetail": "details_bank"
    ]
    @State var fetching = true
    @State var keyState: [String: String]?
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                HStack {
                    HStack {
                        Image(systemName: "arrow.left")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                        Text("Personal Data")
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
                ZStack {
                    Colors.white
                    ScrollView{
                        VStack(spacing: 20) {
                            ForEach(0..<keys.count, id: \.self){ keyIndex in
                                ZStack {
                                    let key = keys[keyIndex]
                                    let title = keyTitles[key]!
                                    let bgImage = keyBGImages[key]!
                                    VStack {
                                        Text(title)
                                            .foregroundColor(Colors.black)
                                            .font(.system(size: 25, weight: .bold))
                                            .shadow(color: Colors.mainColor, radius: 3, x: 2, y: 2)
                                        Spacer()
                                        if fetching {
                                            GIFView(gifName: "cupertino")
                                                .frame(width: 30, height: 30, alignment: .center)
                                        } else {
                                            HStack {
                                                let stateKey = keyState![key]!
                                                let state = states[stateKey]!
                                                let stateText = state[0] as! String
                                                let stateColor = state[1] as! Color
                                                let isPending = stateKey == "pending"
                                                VStack {
                                                    Text(stateText)
                                                        .frame(maxWidth: .infinity, alignment: .leading)
                                                        .foregroundColor(isPending ? Colors.black : Colors.white)
                                                        .padding(10)
                                                }
                                                .frame(maxWidth: .infinity)
                                                .background(stateColor)
                                                .cornerRadius(5)
                                                Button {
                                                    this.listenToClicks(key, this: self)
                                                } label: {
                                                    ZStack {
                                                        Image(systemName: "square.and.pencil")
                                                            .resizable()
                                                            .frame(width: 30, height: 30)
                                                            .foregroundColor(Colors.mainColor)
                                                    }
                                                    .frame(width: 50, height: 50)
                                                    .background(Colors.white)
                                                    .cornerRadius(10)
                                                    .shadow(color: Colors.asher, radius: 4, x: 3, y: 3)
                                                }
                                            }
                                        }
                                    }
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 200)
                                    .backgroundImage(imageName: bgImage, rotateTo: -35, opacity: 0.2)
                                    .padding(10)
                                    .cornerRadius(6)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 6)
                                            .stroke(Colors.ash, lineWidth: 1)
                                    )
                                }
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(10)
                    }
                    .padding(0)
                }
                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
            }
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .onAppear(){
            self.getDataContents()
        }
    }
    
    func getDataContents() {
        guard let url = URL(string: Constants.getDataUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId
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
                        keyState = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [String: String]
                        self.fetching = false
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }
    
    func setData(_ key: String, _ value: String){
        self.keyState![key] = value
    }
    
}
