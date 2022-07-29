//
//  LoginViewController.swift
//  Workruta
//
//  Created by The KING on 04/06/2022.
//

import UIKit
import SwiftUI

class LoginViewController: UIViewController {

    @IBOutlet var controlView: UIView!
    private var models: Models!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.models = Models()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: LoginUIView(this: self, models: models))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }
        
    }
    
    func goBack() {
        self.dismiss(animated: true)
    }
    
    func forgotPassWord() {
        let storyboard = UIStoryboard(name: "Extras", bundle: nil)
        let viewController  = storyboard.instantiateViewController(identifier: "ForgotPassView") as ForgotPassViewController
        viewController.modalPresentationStyle = .fullScreen
        self.present(viewController, animated: true, completion: nil)
    }
    
    func signInUser() {
        let email = models.email.lowercased()
        let password = models.password
        
        if email.isEmpty || password.isEmpty {
                let msg = "Please fill in all fields"
                self.showAlertBox(title: "", msg: msg, btnText: "Close")
                return
        }
        guard let url = URL(string: Constants.signInUrl) else {
            print("URL not found")
            return
        }
        self.models.requesting = true
        let parameters = [
            "email": email,
            "password": password
        ] as [String : Any]
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
                                let dataObj = object["dataStr"] as? NSDictionary
                                let myId = (dataObj?["id"] as? String)!
                                let fName = (dataObj?["fName"] as? String)!
                                let lName = (dataObj?["lName"] as? String)!
                                let photo = Constants.www + (dataObj?["photo"] as? String)!
                                let phone = (dataObj?["phone"] as? String)!
                                let photoSet = (dataObj?["photoSet"] as? Bool)!
                                let name = fName + " " + lName
                                let userParameters = [
                                    "myId": myId,
                                    "name": name,
                                    "email": email,
                                    "photo": photo,
                                    "phoneNumber": phone,
                                    "pictured": photoSet,
                                    "phoneSet": true,
                                    "loggedIn": true,
                                    "phoneVerified": true
                                ] as [String : Any]
                                Functions().loginUser(parameters: userParameters)
                                let storyboard = UIStoryboard(name: "Main", bundle: nil)
                                if photoSet {
                                    let controller = storyboard.instantiateViewController(identifier: "DashboardView") as DashboardViewController
                                    controller.modalPresentationStyle = .fullScreen
                                    self.present(controller, animated: true, completion: nil)
                                } else {
                                    let controller = storyboard.instantiateViewController(identifier: "ChangePhotoView") as ChangePhotoViewController
                                    controller.isBackEnabled = false
                                    controller.modalPresentationStyle = .fullScreen
                                    self.present(controller, animated: true, completion: nil)
                                }
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

}
