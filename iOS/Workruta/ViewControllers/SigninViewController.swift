//
//  SigninViewController.swift
//  Workruta
//
//  Created by The KING on 04/06/2022.
//

import UIKit
import SwiftUI

class SigninViewController: UIViewController {

    @IBOutlet var controlView: UIView!
    private var models: Models!
    weak var timer: Timer?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let this = self
        self.models = Models()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: SigninUIView(this: this, models: models))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
            
            let phoneSet = UserDefaults.standard.bool(forKey: "phoneSet")
            if phoneSet {
                self.models.phoneNumber = UserDefaults.standard.string(forKey: "phoneNumber")!
                self.models.signinViewToScroll = true
                self.models.signinViewCanScroll = true
                let time = self.time()
                let expTime = UserDefaults.standard.integer(forKey: "expTime")
                let stopTime = expTime - Int(time)
                self.startCountDown(stopTime: stopTime)
            }
            
        }

    }
    
    func disableScroll() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.models.signinViewCanScroll = false
        }
    }
    
    func startCountDown(stopTime: Int) {
        let this = self
        let startTime = 0
        var counter = stopTime
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true, block: { _ in
            if stopTime == startTime {
                this.stopCountDown()
                this.models.vCode = ""
                this.models.signinViewToScroll = false
                this.models.signinViewCanScroll = true
                return
            }
            counter -= 1
            this.models.vCodeCountDown = Functions.getTimerStr(counter: counter)
        })
    }
    
    func stopCountDown(){
        timer?.invalidate()
    }
    
    deinit {
        stopCountDown()
    }
    
    func submitCode(this: SigninViewController, parameters: [String: Any]){
        let code: String = parameters["code"] as! String
        if code.count != 6 {
            let msg = "Invalid Verification Code"
            self.showAlertBox(title: "", msg: msg, btnText: "Close")
            return
        }
        guard let url = URL(string: Constants.verifyCodeUrl) else {
            print("URL not found")
            return
        }
        self.models.requesting = true
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.models.requesting = false
                if error != nil {
                    self.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            if noError! {
                                UserDefaults.standard.set(true, forKey: "phoneVerified")
                                let storyboard = UIStoryboard(name: "Main", bundle: nil)
                                let controller = storyboard.instantiateViewController(identifier: "SignupView") as SignupViewController
                                //this.dismiss(animated: false)
                                self.models.signinViewToScroll = false
                                self.models.signinViewCanScroll = true
                                controller.modalPresentationStyle = .fullScreen
                                self.present(controller, animated: true, completion: nil)
                            } else {
                                let dataStr = (object["dataStr"] as? String)!
                                self.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                        }
                    } else {
                        self.showAlertBox(title: "", msg: "No Data received", btnText: "Close")
                    }
                } catch {
                    self.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                }
            }
        }
        urlSession.resume()
    }
    
    func submitNumber(parameters: [String: Any]){
        let phoneNumber: String = parameters["phoneNumber"] as! String
        if phoneNumber.count != 10 || phoneNumber.starts(with: "0") {
            let msg = "Invalid Phone Number"
            self.showAlertBox(title: "", msg: msg, btnText: "Close")
            return
        }
        guard let url = URL(string: Constants.phoneVerifyUrl) else {
            print("URL not found")
            return
        }
        self.models.requesting = true
        let datas = parameters.toQueryString
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = datas.data(using: .utf8)!
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let urlSession = URLSession.shared.dataTask(with: request) { data, _, error in
            DispatchQueue.main.async {
                self.models.requesting = false
                if error != nil {
                    self.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                    return
                }
                do {
                    if let data = data {
                        let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? NSDictionary
                        if let object = json {
                            let noError = object["noError"] as? Bool
                            var dataStr: String
                            if noError! {
                                let dataInt = object["dataStr"] as! Int
                                let stopTime = 60 * 30
                                let expTime = self.time() + Int64(stopTime)
                                dataStr = String(dataInt)
                                self.models.vCode = dataStr
                                self.models.signinViewToScroll = true
                                self.models.signinViewCanScroll = true
                                self.startCountDown(stopTime: stopTime)
                                UserDefaults.standard.set(true, forKey: "phoneSet")
                                UserDefaults.standard.set(expTime, forKey: "expTime")
                                UserDefaults.standard.set(phoneNumber, forKey: "phoneNumber")
                            } else {
                                dataStr = (object["dataStr"] as? String)!
                                self.showAlertBox(title: "", msg: dataStr, btnText: "Close")
                            }
                        }
                    } else {
                        self.showAlertBox(title: "", msg: "No Data received", btnText: "Close")
                    }
                } catch {
                    self.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                }
            }
        }
        urlSession.resume()
    }
    
    static func closeView(this: SigninViewController) {
        this.dismiss(animated: true)
    }

}
