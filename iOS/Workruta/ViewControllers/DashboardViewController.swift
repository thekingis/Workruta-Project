//
//  DashboardViewController.swift
//  Workruta
//
//  Created by The KING on 10/06/2022.
//

import UIKit
import SwiftUI
import CoreLocation

class DashboardViewController: UIViewController, CLLocationManagerDelegate {
    
    @IBOutlet weak var controlView : UIView!
    private var myId: String!
    private var name: String!
    private var photoUrl: String!
    private var imageUrl: URL!
    private var models: Models!
    private var locationMngr = CLLocationManager()

    override func viewDidLoad() {
        super.viewDidLoad()
        self.models = Models()
        myId = UserDefaults.standard.string(forKey: "myId")!
        name = UserDefaults.standard.string(forKey: "name")!
        photoUrl = UserDefaults.standard.string(forKey: "photo")!
        imageUrl = URL(string: photoUrl)
        
        if controlView != nil {
            let childView = UIHostingController(rootView: DashboardUIView(this: self, models: models, name: name, imageUrl: imageUrl))
            addChild(childView)
            childView.view.frame = controlView.bounds
            controlView.addSubview(childView.view)
            
            locationMngr.delegate = self
            locationMngr.desiredAccuracy = kCLLocationAccuracyBest
            locationMngr.requestAlwaysAuthorization()
            if CLLocationManager.locationServicesEnabled(){
                locationMngr.startUpdatingLocation()
            }
            checkNotifiers()
        }
        
    }
    
    func checkNotifiers(){
        guard let url = URL(string: Constants.notifierUrl) else {
            print("URL not found")
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let urlSession = URLSession.shared.dataTask(with: request) { _, _, _ in }
        urlSession.resume()
    }
    
    func openViewController(index: Int){
        let storyboard = UIStoryboard(name: "Dashboards", bundle: nil)
        switch index {
        case 0:
            let viewController  = storyboard.instantiateViewController(identifier: "ProfileView") as ProfileViewController
            viewController.userId = myId
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 1:
            let viewController  = storyboard.instantiateViewController(identifier: "InboxView") as InboxViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 2:
            let viewController  = storyboard.instantiateViewController(identifier: "NotificationsView") as NotificationsViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 4:
            let viewController  = storyboard.instantiateViewController(identifier: "CreateRouteView") as CreateRouteViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 5:
            let viewController  = storyboard.instantiateViewController(identifier: "RouteSearchView") as RouteSearchViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 6:
            let viewController  = storyboard.instantiateViewController(identifier: "PreviousRoutesView") as PreviousRoutesViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 7:
            let viewController  = storyboard.instantiateViewController(identifier: "HistoryView") as HistoryViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 8:
            let viewController  = storyboard.instantiateViewController(identifier: "PaymentsView") as PaymentsViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 9:
            let viewController  = storyboard.instantiateViewController(identifier: "TransactionsView") as TransactionsViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 10:
            let viewController  = storyboard.instantiateViewController(identifier: "SupportView") as SupportViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 11:
            let viewController  = storyboard.instantiateViewController(identifier: "FAQView") as FAQViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        case 12:
            let viewController  = storyboard.instantiateViewController(identifier: "AboutView") as AboutViewController
            viewController.modalPresentationStyle = .fullScreen
            self.present(viewController, animated: true, completion: nil)
        default:
            return
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]){
        let location = locations.last
        let geocoder = CLGeocoder()
        geocoder.reverseGeocodeLocation(location!){ (placemarks, error) in
            if error != nil{
                return
            }
            
            if placemarks != nil {
                let placemark = placemarks! as [CLPlacemark]
                if placemark.count > 0 {
                    let placemark = placemarks![0]
                    let postalCode = placemark.postalCode
                    let thoroughfare = placemark.thoroughfare
                    let subLocality = placemark.subLocality
                    let locality = placemark.locality
                    let administrativeArea = placemark.administrativeArea
                    let country = placemark.country
                    var address = ""
                    if subLocality != nil {
                        address = address + subLocality!
                    }
                    if thoroughfare != nil {
                        if !address.isEmpty {
                            address = address + ", "
                        }
                        address = address + thoroughfare!
                    }
                    if locality != nil {
                        if !address.isEmpty {
                            address = address + ", "
                        }
                        address = address + locality!
                    }
                    if administrativeArea != nil {
                        if !address.isEmpty {
                            address = address + ", "
                        }
                        address = address + administrativeArea!
                    }
                    if country != nil {
                        if !address.isEmpty {
                            address = address + ", "
                        }
                        address = address + country!
                    }
                    if postalCode != nil {
                        if !address.isEmpty {
                            address = address + ", "
                        }
                        address = address + postalCode!
                    }
                    if !address.isEmpty {
                        self.models.userAddress = address
                        self.models.locTxtClr = Colors.black
                        self.models.locIcnClr = Colors.mainColor
                    } else {
                        self.models.userAddress = "Fetching Address..."
                        self.models.locTxtClr = Colors.asher
                        self.models.locIcnClr = Colors.asher
                    }
                } else {
                    self.models.userAddress = "Fetching Address..."
                    self.models.locTxtClr = Colors.asher
                    self.models.locIcnClr = Colors.asher
                }
            }
            
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

}
