//
//  LicenceUIView.swift
//  Workruta
//
//  Created by The KING on 23/06/2022.
//

import SwiftUI

struct LicenceUIView: View {
    
    let this: LicenceViewController
    let paymentsUIView: PaymentsUIView?
    let myId: String
    let cacheUtil = CacheUtil()
    let minDate = Date().addingTimeInterval(-(60 * 60 * 24 * 365 * 5))
    let maxDate = Date().addingTimeInterval(+(60 * 60 * 24 * 365 * 5))
    let date = Date()
    @State var fetching = true
    @State var requesting = false
    @State var imageChanged = false
    @State var showImagePicker = false
    @State var dateFirst = false
    @State var dateSecond = false
    @State var available = false
    @State var showProgressText = false
    @State var showProgressDone = false
    @State var progess: Double = 0
    @State var licenceNo = ""
    @State var licenceCat = ""
    @State var classIndex = 0
    @State var stateIndex = 0
    @State var isDate = ""
    @State var exDate = ""
    @State var filePath = ""
    @State var password = ""
    @State var iLicenceNo = ""
    @State var iLicenceCat = ""
    @State var iClassIndex = 0
    @State var iStateIndex = 0
    @State var iIsDate = ""
    @State var iExDate = ""
    @State var iFilePath = ""
    @State var progressPercent = ""
    @State var iDate = "Select Date"
    @State var eDate = "Select Date"
    @State var uiImage = UIImage()
    @State private var selectedDate = Date()
    @State private var issDate = Date()
    @State private var expDate = Date()
    
    var body: some View {
        ZStack {
            Colors.white
            ScrollView{
                VStack(spacing: 20) {
                    TextField("", text: $licenceNo)
                        .modifier(PlaceholderStyle(show: licenceNo.isEmpty, text: Strings.licence_number)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                    VStack(alignment: .leading) {
                        Text(Strings.licence_class).foregroundColor(Colors.mainColor).font(.system(size: 17)).padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                        Picker("", selection: $classIndex){
                            ForEach(0..<Constants.classes.count, id: \.self){ index in
                                Text(Constants.classes[index])
                            }
                        }
                        .customPickerStyle(index: $classIndex, items: Constants.classes, font: .system(size: 17), padding: 10)
                        .frame(width: UIScreen.main.bounds.width - 50)
                        .overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        .disabled(requesting)
                    }
                    VStack(alignment: .leading) {
                        Text(Strings.issued_state).foregroundColor(Colors.mainColor).font(.system(size: 17)).padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                        Picker("", selection: $stateIndex){
                            ForEach(0..<Constants.allStates.count, id: \.self){ index in
                                Text(Constants.allStates[index]).foregroundColor(Colors.black).font(.system(size: 17))
                            }
                        }
                        .customPickerStyle(index: $stateIndex, items: Constants.allStates, font: .system(size: 17), padding: 10)
                        .frame(width: UIScreen.main.bounds.width - 50)
                        .overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
                        .disabled(requesting)
                    }
                    VStack(alignment: .leading) {
                        Text(Strings.issued_date).foregroundColor(Colors.mainColor).font(.system(size: 17)).padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                        TextField("", text: $iDate){
                            UIApplication.shared.hideKeyboard(hide: false)
                        }.padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.asher).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(true).onTapGesture {
                            UIApplication.shared.hideKeyboard(hide: true)
                            if !requesting {
                                dateFirst.toggle()
                            }
                        }
                    }
                    VStack(alignment: .leading) {
                        Text(Strings.expiry_date).foregroundColor(Colors.mainColor).font(.system(size: 17)).padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                        TextField("", text: $eDate){
                            UIApplication.shared.hideKeyboard(hide: false)
                        }.padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.asher).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(true).onTapGesture {
                            UIApplication.shared.hideKeyboard(hide: true)
                            if !requesting {
                                dateSecond.toggle()
                            }
                        }
                    }
                    VStack(alignment: .leading) {
                        Text(Strings.licence_category).foregroundColor(Colors.mainColor).font(.system(size: 17)).padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                        RadioButtonGroup(items: Constants.types, direction: "horizontal", selectedId: $licenceCat) { selected in
                            licenceCat = selected
                        }.padding(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 0))
                    }
                    VStack(alignment: .center) {
                        Text(Strings.camera_note).foregroundColor(Colors.red).font(.system(size: 15)).padding(EdgeInsets(top: 0, leading: 30, bottom: 0, trailing: 30)).multilineTextAlignment(.leading)
                        ZStack{
                            Image(systemName: "camera.fill")
                                .resizable()
                                .frame(width: 30, height: 25)
                                .foregroundColor(Colors.mainColor)
                            Image(uiImage: uiImage)
                                .resizable()
                                .scaledToFill()
                                .frame(width: UIScreen.main.bounds.width - 25, height: (UIScreen.main.bounds.width - 25) * 1.33)
                        }
                        .frame(width: UIScreen.main.bounds.width - 20, height: (UIScreen.main.bounds.width - 20) * 1.33)
                        .overlay(Rectangle().stroke(Colors.mainColor, style: StrokeStyle(lineWidth: 2, dash: [5])))
                        .onTapGesture {
                            UIApplication.shared.hideKeyboard(hide: true)
                            if !requesting {
                                showImagePicker.toggle()
                            }
                        }
                    }
                    SecureField("", text: $password)
                        .modifier(PlaceholderStyle(show: password.isEmpty, text: Strings.password)).padding(10).background(Colors.white).cornerRadius(10).foregroundColor(Colors.black).font(.system(size: 17)).frame(width: UIScreen.main.bounds.width - 50).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0)).disabled(requesting)
                }
                .padding(top: 20, bottom: 80)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(top: 40)
            if fetching {
                ZStack{
                    Colors.whiteFade
                    GIFView(gifName: "cupertino")
                        .frame(width: 30, height: 30, alignment: .center)
                }
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
        .overlay(
            HStack(alignment: .center, spacing: 10){
                (Text(Image(systemName: "chevron.left")) + Text(Strings.driver_s_licence_infomation))
                    .foregroundColor(Colors.white).padding(10).font(.system(size: 18))
                    .onTapGesture {
                        if !requesting {
                            this.finish()
                        }
                    }
                Spacer()
            }
                .background(Colors.mainColor)
            , alignment: .topLeading
        )
        .overlay(
            HStack(alignment: .center){
                Spacer()
                Button {
                    UIApplication.shared.hideKeyboard(hide: true)
                    if !requesting && !fetching {
                        self.requesting = true
                        saveData()
                    }
                } label: {
                    Text(Strings.save).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                }.background(Colors.mainColor).border(Colors.white, width: 1).cornerRadius(10.0).overlay(RoundedRectangle(cornerRadius: 10.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }.padding(5).background(Colors.white)
            , alignment: .bottom
        )
        .overlay(
            ZStack {
                if requesting {
                    Colors.whiteFade
                    CircularProgressView(progress: progess)
                        .frame(width: 120, height: 120)
                        .background(Colors.whiteFade)
                        .cornerRadius(60)
                        .contentShape(Circle())
                    if showProgressText{
                        Text(progressPercent)
                            .foregroundColor(Colors.green)
                            .font(.system(size: 35))
                    }
                    if showProgressDone {
                        Image(systemName: "checkmark")
                            .resizable()
                            .foregroundColor(Colors.green)
                            .frame(width: 50, height: 50)
                    }
                }
            }
            , alignment: .topLeading
        )
        .onTapGesture {
            UIApplication.shared.hideKeyboard(hide: true)
        }
        .onAppear(){
            getData()
        }
        .sheet(isPresented: $dateFirst) {
            ZStack{
                Colors.blackFade
                DatePicker("", selection: $selectedDate, in: minDate...date, displayedComponents: [.date])
                    .datePickerStyle(GraphicalDatePickerStyle())
                    .frame(width: UIScreen.main.bounds.width - 30, height: UIScreen.main.bounds.width - 30)
                    .background(Colors.white)
                    .colorScheme(.light)
                    .cornerRadius(7)
                    .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }
            .overlay(
                HStack(alignment: .center){
                    Spacer()
                    Button {
                        issDate = selectedDate
                        iDate = selectedDate.toShortString()
                        dateFirst.toggle()
                    } label: {
                        Text(Strings.ok).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    }.background(Colors.mainColor).cornerRadius(7.0).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                    Spacer()
                }
                    .padding(20)
                , alignment: .bottomLeading
            )
        }
        .sheet(isPresented: $dateSecond) {
            ZStack{
                Colors.blackFade
                DatePicker("", selection: $selectedDate, in: date...maxDate, displayedComponents: [.date])
                    .datePickerStyle(GraphicalDatePickerStyle())
                    .frame(width: UIScreen.main.bounds.width - 30, height: UIScreen.main.bounds.width - 30)
                    .background(Colors.white)
                    .colorScheme(.light)
                    .cornerRadius(7)
                    .overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
            }
            .overlay(
                HStack(alignment: .center){
                    Spacer()
                    Button {
                        expDate = selectedDate
                        eDate = selectedDate.toShortString()
                        dateSecond.toggle()
                    } label: {
                        Text(Strings.ok).foregroundColor(Colors.white).font(.system(size: 18)).padding(EdgeInsets(top: 15, leading: 25, bottom: 15, trailing: 25))
                    }.background(Colors.mainColor).cornerRadius(7.0).overlay(RoundedRectangle(cornerRadius: 7.0).stroke(Colors.mainColor, lineWidth: 1.0))
                    Spacer()
                }
                    .padding(20)
                , alignment: .bottomLeading
            )
    }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(sourceType: .camera) { image in
                imageChanged = true
                uiImage = image
            }
        }
    }
    
    func saveData() {
        let parameters = getParameters()
        if licenceNo == "" || classIndex == 0 || stateIndex == 0 || licenceCat == "" || isDate == "" || exDate == "" || password == "" {
            this.showAlertBox(title: "", msg: "Please fill in all fields", btnText: "Close")
            self.requesting = false
            return
        }
        if !available && uiImage.cgImage == nil && uiImage.ciImage == nil {
            this.showAlertBox(title: "", msg: "Please take a picture of your driver's licence", btnText: "Close")
            self.requesting = false
            return
        }
        if licenceNo == iLicenceNo && licenceCat == iLicenceCat && classIndex == iClassIndex && stateIndex == iStateIndex && isDate == iIsDate && exDate == iExDate && !imageChanged {
            this.showAlertBox(title: "", msg: "No changes made", btnText: "Close")
            self.requesting = false
            return
        }
        guard let url = URL(string: Constants.submitLicenceUrl) else {
            print("URL not found")
            return
        }
        
        self.progess = 0
        self.progressPercent = "0%"
        self.showProgressText = true
        var observation: NSKeyValueObservation?
        var request: URLRequest
        if !imageChanged {
            let datas = parameters.toQueryString
            request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.httpBody = datas.data(using: .utf8)!
            request.addValue("application/json", forHTTPHeaderField: "Accept")
        } else {
            let imageData = Data((uiImage.fixedOrientation()!.pngData())!)
            let rqst = NSMutableURLRequest(url: url)
            rqst.httpMethod = "POST"
            rqst.cachePolicy = .reloadIgnoringLocalCacheData
            let boundary = "unique-consistent-string"
            rqst.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
            let body = NSMutableData()
            for (key, value) in parameters {
                body.append("--\(boundary)\r\n".data(using: String.Encoding.utf8)!)
                body.append("Content-Disposition: form-data; name=\(key)\r\n\r\n".data(using: String.Encoding.utf8)!)
                body.append("\(value)\r\n".data(using: String.Encoding.utf8)!)
                body.append("--\(boundary)\r\n".data(using: String.Encoding.utf8)!)
            }
            body.append("--\(boundary)\r\n".data(using: String.Encoding.utf8)!)
            body.append("Content-Disposition: form-data; name=\("file"); filename=imageName.jpg\r\n".data(using: String.Encoding.utf8)!)
            body.append("Content-Type: image/jpeg\r\n\r\n".data(using: String.Encoding.utf8)!)
            body.append(imageData as Data)
            body.append("\r\n".data(using: String.Encoding.utf8)!)
            body.append("--\(boundary)--\r\n".data(using: String.Encoding.utf8)!)
            rqst.httpBody = body as Data
            request = rqst as URLRequest
        }
        
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 300.0
        config.timeoutIntervalForResource = 300.0
        let urlSession = URLSession(configuration: config)
        
        let task = urlSession.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                observation?.invalidate()
                if error != nil {
                    self.requesting = false
                    self.this.showAlertBox(title: "", msg: "Failed to complete request", btnText: "Close")
                    print("Error")
                    return
                }
                do {
                    if let data = data {
                        //print("Data: \(String(decoding: data, as: UTF8.self))")
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as! Bool
                            if noError {
                                imageChanged = false
                                let dataStr = object["dataStr"] as! NSDictionary
                                self.filePath = dataStr["filePath"] as! String
                                self.iLicenceNo = self.licenceNo
                                self.iLicenceCat = self.licenceCat
                                self.iClassIndex = self.classIndex
                                self.iStateIndex = self.stateIndex
                                self.iIsDate = self.isDate
                                self.iExDate = self.exDate
                                self.iFilePath = self.filePath
                                if self.paymentsUIView != nil {
                                    self.paymentsUIView!.setData("licenceDetail", "pending")
                                }
                            } else {
                                let dataStr = object["dataStr"] as! String
                                self.this.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                        }
                    }
                } catch let myJSONError {
                    self.requesting = false
                    self.this.showAlertBox(title: "", msg: "Error Collecting Data", btnText: "Close")
                    print(myJSONError)
                }
            }
        }
        observation = task.progress.observe(\.fractionCompleted) {observationProgress, _ in
            DispatchQueue.main.async {
                let progress = observationProgress.fractionCompleted
                let progressPercent = Int(progress * 100)
                self.progess = progress
                self.progressPercent = String(progressPercent) + "%"
                if self.progess == 1 {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0){
                        self.showProgressText = false
                        self.showProgressDone = true
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.0){
                        self.showProgressDone = false
                        self.requesting = false
                        self.this.showAlertBox(title: "", msg: "Data Saved", btnText: "Close")
                    }
                }
            }
        }
        task.resume()
    }
    
    func getData() {
        guard let url = URL(string: Constants.editorUrl) else {
            print("URL not found")
            return
        }
        let parameters: [String: String] = [
            "user": myId,
            "cat": "licence"
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
                            self.available = object["available"] as! Bool
                            if available {
                                let formatter = DateFormatter()
                                formatter.dateFormat = "yyyy-MM-dd"
                                self.licenceNo = object["licenceNo"] as! String
                                self.licenceCat = object["licenceCat"] as! String
                                self.classIndex = Int(object["classIndex"] as! String)!
                                self.stateIndex = Int(object["stateIndex"] as! String)!
                                self.isDate = object["isDate"] as! String
                                self.exDate = object["exDate"] as! String
                                self.filePath = object["filePath"] as! String
                                self.iLicenceNo = self.licenceNo
                                self.iLicenceCat = self.licenceCat
                                self.iClassIndex = self.classIndex
                                self.iStateIndex = self.stateIndex
                                self.iIsDate = self.isDate
                                self.iExDate = self.exDate
                                self.iFilePath = self.filePath
                                self.issDate = formatter.date(from: self.isDate)!
                                self.expDate = formatter.date(from: self.exDate)!
                                self.iDate = self.issDate.toShortString()
                                self.eDate = self.expDate.toShortString()
                                let imageURL = URL(string: Constants.www + self.filePath)!
                                getLicenceImage(imageURL: imageURL)
                            }
                        }
                    }
                } catch let myJSONError {
                    print(myJSONError)
                }
                self.fetching = false
            }
        }
        urlSession.resume()
    }
    
    func getParameters() -> [String: String] {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        isDate = formatter.string(from: issDate)
        exDate = formatter.string(from: expDate)
        let isSplit = isDate.split(separator: "-")
        let exSplit = exDate.split(separator: "-")
        var parameter: [String: String] = [
            "user": myId,
            "password": password,
            "licenceNum": licenceNo,
            "category": licenceCat,
            "classIndex": String(classIndex),
            "stateIndex": String(stateIndex),
            "yearIsIndex": String(isSplit[0]),
            "mnthIsIndex": String(isSplit[1]),
            "dayIsIndex": String(isSplit[2]),
            "yearExIndex": String(exSplit[0]),
            "mnthExIndex": String(exSplit[1]),
            "dayExIndex": String(exSplit[2])
        ]
        if !imageChanged {
            parameter["filePath"] = filePath
        }
        return parameter
    }
    
    func getLicenceImage(imageURL: URL){
        cacheUtil.getImage(imageURL: imageURL) { data, error in
            if let data = data {
                uiImage = UIImage(data: data)!
            }
        }
    }
    
}
