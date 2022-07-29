//
//  ChangePhotoViewController.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import UIKit
import SwiftUI
import Photos
import FirebaseDatabase

class ChangePhotoViewController: UIViewController {

    @IBOutlet weak var controlView : UIView!
    var isBackEnabled: Bool!
    private var name: String!
    private var safeEmail: String!
    private var models: Models!
    private var myId: Int!
    private var photoUrl: URL!
    var imagePickerController = UIImagePickerController()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let this = self
        self.models = Models()
        myId = UserDefaults.standard.integer(forKey: "myId")
        safeEmail = UserDefaults.standard.string(forKey: "email")!.safeEmail()
        name = UserDefaults.standard.string(forKey: "name")!
        let photo = UserDefaults.standard.string(forKey: "photo")
        if photo != nil && !photo!.isEmpty {
            photoUrl = URL(string: photo!)
        }
        
        if controlView != nil {
            let childView = UIHostingController(rootView: ChangePhotoUIView(this: this, models: models, name: name, isBackEnabled: isBackEnabled, photoUrl: photoUrl))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
        }
        
        
    }
    
    func logoutUser() {
        Functions().removeAllUserCaches()
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0){
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
            let controller = storyboard.instantiateViewController(identifier: "StartView") as StartViewController
            controller.modalPresentationStyle = .fullScreen
            self.present(controller, animated: true, completion: nil)
        }
    }
    
    func proceedBackward() {
        self.dismiss(animated: true)
    }
    
    func proceedForward() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let controller = storyboard.instantiateViewController(identifier: "WelcomeView") as WelcomeViewController
        controller.modalPresentationStyle = .fullScreen
        self.present(controller, animated: true, completion: nil)
    }
    
    func saveImage(selectedImage: UIImage?) {
        if selectedImage == nil {
            showAlertBox(title: "", msg: "Please Take or Select a Photo ", btnText: "Close")
             return
        }
        self.models.progess = 0.0
        self.models.progressPercent = "0%"
        self.models.showProgressText = true
        self.models.requesting = true
        var observation: NSKeyValueObservation?
        let imageData = Data((selectedImage?.pngData())!)
        let request = NSMutableURLRequest(url: URL(string: Constants.changePhotoUrl)!)
        request.httpMethod = "POST"
        request.cachePolicy = .reloadIgnoringLocalCacheData
        let boundary = "unique-consistent-string"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        let body = NSMutableData()
        body.append("--\(boundary)\r\n".data(using: String.Encoding.utf8)!)
        body.append("Content-Disposition: form-data; name=\("id")\r\n\r\n".data(using: String.Encoding.utf8)!)
        body.append("\(String(myId))\r\n".data(using: String.Encoding.utf8)!)
        body.append("--\(boundary)\r\n".data(using: String.Encoding.utf8)!)
        body.append("--\(boundary)\r\n".data(using: String.Encoding.utf8)!)
        body.append("Content-Disposition: form-data; name=\("file"); filename=imageName.jpg\r\n".data(using: String.Encoding.utf8)!)
        body.append("Content-Type: image/jpeg\r\n\r\n".data(using: String.Encoding.utf8)!)
        body.append(imageData as Data)
        body.append("\r\n".data(using: String.Encoding.utf8)!)
        body.append("--\(boundary)--\r\n".data(using: String.Encoding.utf8)!)
        request.httpBody = body as Data
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 300.0
        config.timeoutIntervalForResource = 300.0
        let urlSession = URLSession(configuration: config)
        let task = urlSession.dataTask(with: request as URLRequest) { (data, response, error) in
            DispatchQueue.main.async {
                observation?.invalidate()
                if error != nil {
                    print(error?.localizedDescription ?? "")
                    self.models.requesting = false
                    self.showAlertBox(title: "", msg: "An Error occured. Please try again", btnText: "Close")
                    return
                }
                if let data = data {
                    let database = Database.database().reference()
                    let userDB = database.child("\(self.safeEmail!)/photoUrl")
                    let photoUrl = String(decoding: data, as: UTF8.self)
                    let photoURL = Constants.www + photoUrl
                    UserDefaults.standard.set(true, forKey: "pictured")
                    UserDefaults.standard.set(photoURL, forKey: "photo")
                    userDB.setValue(photoUrl.safeUrl())
                } else {
                    self.models.requesting = false
                    self.showAlertBox(title: "", msg: "No Data received", btnText: "Close")
                }
            }
        }
        observation = task.progress.observe(\.fractionCompleted) {observationProgress, _ in
            DispatchQueue.main.async {
                let progress = observationProgress.fractionCompleted
                let progressPercent = Int(progress * 100)
                self.models.progess = progress
                self.models.progressPercent = String(progressPercent) + "%"
                if self.models.progess == 1 {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0){
                        self.models.showProgressText = false
                        self.models.showProgressDone = true
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.0){
                        self.models.showProgressDone = false
                        self.models.showPhotoNext = true
                        self.models.requesting = false
                    }
                }
            }
        }
        task.resume()

    }
    
    func checkPermission() {
        if PHPhotoLibrary.authorizationStatus() != PHAuthorizationStatus.authorized {
            PHPhotoLibrary.requestAuthorization({(statust: PHAuthorizationStatus) -> Void in ()})
        }
        if PHPhotoLibrary.authorizationStatus() != PHAuthorizationStatus.authorized {
            PHPhotoLibrary.requestAuthorization(requestAuthorizationHandler)
        }
    }
    
    func requestAuthorizationHandler(status: PHAuthorizationStatus) {
        if PHPhotoLibrary.authorizationStatus() == PHAuthorizationStatus.authorized {
            showAlertBox(title: "", msg: "Please Authorize Workruta to Access Your Photo Library", btnText: "Close")
        }
    }

}
