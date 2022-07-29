//
//  Models.swift
//  Workruta
//
//  Created by The KING on 09/06/2022.
//

import SwiftUI
import Foundation

class Models: ObservableObject {
    @Published var locIcnClr = Colors.asher
    @Published var locTxtClr = Colors.asher
    @Published var requesting = false
    @Published var showPhotoNext = false
    @Published var showProgressText = false
    @Published var showProgressDone = false
    @Published var signinViewCanScroll = false
    @Published var signinViewToScroll = false
    @Published var changedPhoto = false
    @Published var progess = 0.0
    @Published var latitude = 0.0
    @Published var longitude = 0.0
    @Published var userAddress: String = "Fetching Address..."
    @Published var progressPercent: String = ""
    @Published var phoneNumber: String = ""
    @Published var photo: String = ""
    @Published var vCode: String = ""
    @Published var fName: String = ""
    @Published var lName: String = ""
    @Published var email: String = ""
    @Published var password: String = ""
    @Published var conPass: String = ""
    @Published var gender: String = ""
    @Published var address: String = ""
    @Published var vCodeCountDown: String = Strings._00_00
}
