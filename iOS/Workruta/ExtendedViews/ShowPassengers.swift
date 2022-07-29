//
//  ShowPassengers.swift
//  Workruta
//
//  Created by The KING on 27/07/2022.
//

import SwiftUI

struct ShowPassengers: View {
    
    let routesUIView: RoutesUIView
    let routeId: String
    @State var requesting = true
    @State var coordinates: [[String]]!
    @State var dataObject = [[String: Any]]()
    
    var body: some View {
        ZStack{
            Colors.blackFade
            VStack(alignment: .center, spacing: 20) {
                if requesting {
                    Spacer()
                    HStack {
                        Spacer()
                        VStack {
                            GIFView(gifName: "searching")
                                .frame(width: 300, height: 225, alignment: .center)
                            Text(Strings.searching_for_matching_routes).foregroundColor(Colors.asher)
                        }
                        Spacer()
                    }
                    Spacer()
                } else {
                    if dataObject.count == 0 {
                        Spacer()
                        HStack {
                            Spacer()
                            Text("No match found")
                                .foregroundColor(Colors.black)
                                .font(.system(size: 16, weight: .bold))
                            Spacer()
                        }
                        Spacer()
                    } else {
                        ScrollView {
                            VStack(spacing: 20) {
                                ForEach(0..<dataObject.count, id: \.self){ index in
                                    SearchRouteUIView(previousRoutesUIView: nil, routeSearchUIView: nil, routesUIView: routesUIView, thisIndex: 3, searchData: dataObject[index] , coordinates: coordinates, directly: true)
                                }
                            }
                            .padding(5)
                        }
                        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
                    }
                }
            }
            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
            .background(Colors.white)
            .cornerRadius(7)
            .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.white, lineWidth: 0.0))
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .padding(10)
        .onAppear(){
            getPassengers()
        }
    }
    
    func getPassengers(){
        guard let url = URL(string: Constants.showRoutersUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "routeId": routeId
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
                            self.coordinates = object["coordinates"] as? [[String]]
                            self.dataObject = object["data"] as! [[String: Any]]
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
}
