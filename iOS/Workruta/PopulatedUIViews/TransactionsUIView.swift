//
//  TransactionsUIView.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import SwiftUI

struct TransactionsUIView: View {
    
    let this: TransactionsViewController
    let myId = UserDefaults.standard.string(forKey: "myId")!
    @State var requesting = true
    @State var dataObj: [[String: Any]] = []
    
    var body: some View {
        ZStack {
            Colors.white
            VStack(spacing: 0) {
                HStack {
                    HStack {
                        Image(systemName: "arrow.left")
                            .foregroundColor(Colors.white)
                            .font(.system(size: 18, weight: .bold))
                        Text("Transactions")
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
                    if dataObj.count > 0 {
                        ScrollView{
                            VStack {
                                ForEach(0..<dataObj.count, id: \.self){ index in
                                    ZStack {
                                        let data = dataObj[index]
                                        let fromMe = data["fromMe"] as! Bool
                                        let name = data["name"] as! String
                                        let amount = data["amount"] as! String
                                        let date = data["date"] as! String
                                        let dateStr = date.convertToDate().minify()
                                        let dollar = amount.stripeToDollar()
                                        let colorToAsh = (index % 2) == 0
                                        HStack {
                                            Image(systemName: fromMe ? "arrow.up.square" : "arrow.down.square")
                                                .resizable()
                                                .scaledToFit()
                                                .frame(width: 40, height: 40)
                                                .foregroundColor(fromMe ? Colors.normalRed : Colors.green)
                                            VStack(spacing: 10) {
                                                Text(name)
                                                    .frame(maxWidth: .infinity, alignment: .leading)
                                                    .foregroundColor(Colors.black)
                                                    .font(.system(size: 16, weight: .bold))
                                                    .lineLimit(1)
                                                HStack {
                                                    Text(dollar)
                                                        .frame(maxWidth: .infinity, alignment: .leading)
                                                        .foregroundColor(fromMe ? Colors.normalRed : Colors.green)
                                                        .font(.system(size: 14))
                                                    Text(dateStr)
                                                        .foregroundColor(Colors.asher)
                                                        .font(.system(size: 13))
                                                }
                                            }
                                        }
                                        .frame(maxWidth: .infinity)
                                        .padding(10)
                                        .background(colorToAsh ? Colors.ash : Colors.white)
                                        .cornerRadius(7)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 7)
                                                .stroke(Colors.asher, lineWidth: 1)
                                        )
                                    }
                                    .frame(maxWidth: .infinity)
                                }
                            }
                            .padding(10)
                        }
                        .padding(0)
                    }
                    if requesting {
                        ZStack {
                            Colors.whiteFade
                            GIFView(gifName: "loader")
                                .frame(width: 30, height: 30, alignment: .center)
                        }
                    }
                    if dataObj.count == 0 && !requesting {
                        Text("No transaction made")
                            .foregroundColor(Colors.black)
                            .font(.system(size: 16, weight: .bold))
                    }
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
        guard let url = URL(string: Constants.transactionsUrl) else {
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
                self.requesting = false
                if error != nil {
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                    //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [[String: Any]]
                        if let object = json {
                            self.dataObj = object
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
            }
        }
        urlSession.resume()
    }
}
