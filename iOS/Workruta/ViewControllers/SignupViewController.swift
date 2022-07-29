//
//  SignupViewController.swift
//  Workruta
//
//  Created by The KING on 04/06/2022.
//

import UIKit
import SwiftUI
import GooglePlaces
import FirebaseDatabase

class SignupViewController: UIViewController {

    @IBOutlet var controlView: UIView!
    private var models: Models!
    let autocompleteCtrller = GMSAutocompleteViewController()
    let filter = GMSAutocompleteFilter()
    let fields: GMSPlaceField = [.name, .coordinate]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let this = self
        self.models = Models()
        
        if controlView != nil {
            let childView = UIHostingController(rootView: SignupUIView(this: this, models: models))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
            
            filter.type = .establishment
            filter.countries = [Constants.country]
            autocompleteCtrller.delegate = self
            autocompleteCtrller.autocompleteFilter = filter
            autocompleteCtrller.placeFields = fields
            autocompleteCtrller.primaryTextColor = UIColor.black
            autocompleteCtrller.secondaryTextColor = UIColor.lightGray
            autocompleteCtrller.tableCellSeparatorColor = UIColor.lightGray
            autocompleteCtrller.tableCellBackgroundColor = UIColor.white
            UITextField.appearance(whenContainedInInstancesOf: [UISearchBar.self]).defaultTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor.black]
            
        }
        
    }
    
    func openAutoSuggest() {
        self.present(autocompleteCtrller, animated: true, completion: nil)
    }
    
    func changeNumber() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let controller = storyboard.instantiateViewController(identifier: "SigninView") as SigninViewController
        controller.modalPresentationStyle = .fullScreen
        self.present(controller, animated: true, completion: nil)
    }
    
    func signUpUser() {
        let phoneNumber = UserDefaults.standard.string(forKey: "phoneNumber")!
        let fName = models.fName
        let lName = models.lName
        let email = models.email.lowercased()
        let password = models.password
        let conPass = models.conPass
        let gender = models.gender
        let address = models.address
        let latitude = models.latitude
        let longitude = models.longitude
        
        if fName.isEmpty || lName.isEmpty || email.isEmpty || password.isEmpty || conPass.isEmpty || gender.isEmpty || address.isEmpty {
                let msg = "Please fill in all fields"
                self.showAlertBox(title: "", msg: msg, btnText: "Close")
                return
        }
        guard let url = URL(string: Constants.signUpUrl) else {
            print("URL not found")
            return
        }
        self.models.requesting = true
        let parameters = [
            "phoneNumber": phoneNumber,
            "fName": fName,
            "lName": lName,
            "email": email,
            "password": password,
            "conPass": conPass,
            "gender": gender,
            "address": address,
            "latitude": latitude,
            "longitude": longitude
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
                                let myId = (object["dataStr"] as? Int)!
                                let name = fName + " " + lName
                                UserDefaults.standard.set(myId, forKey: "myId")
                                UserDefaults.standard.set(name, forKey: "name")
                                UserDefaults.standard.set(email, forKey: "email")
                                UserDefaults.standard.set(true, forKey: "loggedIn")
                                let database = Database.database().reference()
                                let userDB = database.child(email.safeEmail())
                                let value: [String: String] = [
                                    "name": name,
                                    "userId": String(myId)
                                ]
                                userDB.setValue(value)
                                let storyboard = UIStoryboard(name: "Main", bundle: nil)
                                let controller = storyboard.instantiateViewController(identifier: "ChangePhotoView") as ChangePhotoViewController
                                //this.dismiss(animated: false)
                                controller.isBackEnabled = false
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
}

extension SignupViewController: GMSAutocompleteViewControllerDelegate {
    func viewController(_ viewController: GMSAutocompleteViewController, didAutocompleteWith place: GMSPlace) {
        models.address = place.name!
        models.latitude = place.coordinate.latitude
        models.longitude = place.coordinate.longitude
        dismiss(animated: true, completion: nil)
    }
    
    func viewController(_ viewController: GMSAutocompleteViewController, didFailAutocompleteWithError error: Error) {
    }
    
    func wasCancelled(_ viewController: GMSAutocompleteViewController) {
        dismiss(animated: true, completion: nil)
    }
    
    func didRequestAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
    }
    
    func didUpdateAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
    }
}

