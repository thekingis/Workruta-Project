//
//  Constants.swift
//  Workruta
//
//  Created by The KING on 08/06/2022.
//

import Foundation


class Constants {
    public static let days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
    public static let months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    public static let allStates = ["--Select State--", "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"]
    public static let allBanks = ["--Select Bank--", "First Republic Bank", "Bank OZK", "City National Bank", "Zions Bancorporation", "Signature Bank", "JPMorgan Chase", "Bank of America", "Citigroup", "Wells Fargo", "Goldman Sachs", "Morgan Stanley", "Charles Schwab Corporation", "U.S. Bancorp", "PNC Financial Services", "Truist Financial", "TD Bank, N.A.", "The Bank of New York Mellon", "Capital One", "Teachers Insurance and Annuity Association of America|TIAA", "State Street Corporation", "HSBC Bank USA", "USAA", "Fifth Third Bank", "BMO Harris Bank", "Silicon Valley Bank|SVB Financial Group", "UBS", "Citizens Financial Group", "KeyCorp", "American Express", "Ally Financial", "Huntington Bancshares", "Barclays", "Ameriprise", "Northern Trust", "MUFG Union Bank", "RBC Bank", "BNP Paribas", "Bank of the West", "Regions Financial Corporation", "Santander Bank", "M&T Bank", "Deutsche Bank", "Discover Financial", "Credit Suisse", "Comerica", "Synchrony Financial", "First Horizon National Corporation", "Popular, Inc.", "People's United Financial", "Raymond James Financial", "East West Bank", "CIBC Bank USA", "New York Community Bank", "First Citizens BancShares", "Synovus", "CIT Group", "Western Alliance Bank", "Mizuho Financial Group", "Frost Bank|Cullen/Frost Bankers, Inc.", "Wintrust Financial", "BOK Financial Corporation", "John Deere Capital Corporation", "Valley National Bank", "South State Bank", "FNB Corporation", "UMB Financial Corporation", "Pinnacle Financial Partners", "Prosperity Bancshares", "Texas Capital Bank", "PacWest Bancorp", "Webster Bank", "Hancock Whitney", "BankUnited", "Commerce Bancshares", "Associated Banc-Corp", "MidFirst Bank", "Umpqua Holdings Corporation", "Stifel", "Sterling Bancorp", "United Bank (West Virginia)", "FirstBank Holding Co", "Investors Bank", "Flagstar Bank", "Arvest Bank", "Fulton Financial Corporation", "First National of Nebraska", "First Hawaiian Bank", "SMBC Americas Holdings Inc.", "Old National Bank", "Simmons Bank", "Bank of Hawaii", "Ameris Bancorp", "First Midwest Bank", "Glacier Bancorp Inc.", "First BanCorp", "Pacific Premier Bancorp, Inc.", "BCI Financial Group, Inc.", "Atlantic Union Bank", "Cathay Bank", "Cadence Bank", "Washington Federal", "Central Bancompany, Inc", "United Community Bank", "First Interstate Bancsystem, Inc.", "Customers Bancorp, Inc", "EB Acquisition Company LLC"]
    public static let allCars = ["--Select Product--", "Abarth", "Acura", "Alfa Romeo", "Aston Martin", "Audi", "Bentley", "BMW", "Buick", "Cadillac", "Chevrolet", "Chrysler", "Citreon", "Dacia", "Dodge", "Ferrari", "Fiat", "Ford", "GMC", "Honda", "Hummer", "Hyundai", "Infiniti", "Isuzu", "Jaguar", "Jeep", "Kia", "Lamborghini", "Lancia", "Land Rover", "Lexus", "Lincoln", "Lotus", "Maserati", "Mazda", "Mercedes-Benz", "Mercury", "Mini", "Mitsubishi", "Nissan", "Opel", "Peugeot", "Pontiac", "Porsche", "Racer", "Ram", "Renault", "Saab", "Saturn", "Scion", "Seat", "Skoda", "Smart", "SsangYong", "Subaru", "Suzuki", "Tesla", "Toyota", "Volkswagen", "Volvo", "Wiesmann"]
    public static let classes = ["--Select Class--", "A", "B", "C", "D", "E", "F", "G", "H", "J", "V"]
    public static let types = ["Motorcycle", "Private", "General"]
    public static let validIDs = ["--Choose ID--", "National ID", "Driver's Licence", "International Passport"]
    public static let country = "US"
    public static let stripeSKTestAPIKey = "sk_test_51L6UkaDr3vMd0jcT0dD0qx4WNg7vQaeOuSyNJIBmcLhZIUX7RFGuawRHti57arqAaL7MQReuOphLDRPgui0gEuil002IPOV1Sv"
    public static let stripePKTestAPIKey = "pk_test_51L6UkaDr3vMd0jcT6EbNrz5WVWtJe4Xwv6iRrgUTCQKx42TSBPCwVX5H8kW51F7xcmpb4hRcbhxquMp8eRgLagt100e1AfO3D2"
    public static let stripeSKLiveAPIKey = "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"
    public static let stripePKLiveAPIKey = "pk_live_51L6UkaDr3vMd0jcTdVlNnoBzraA66rLsIKDENPNgTdj53n1P6aGbTJZj8GJaW7kqO1aeimofa2bSCNvYYfDk2x8500zTlrKxJH"
    public static let mapAPIKey = "AIzaSyDJjJAGxA82xd8FlatRWI14GqGRHZr5BSo"
    public static let socketUrl = "http://34.127.44.170:8080/"
    public static let www = "http://workruta.ng/"
    private static let rootUrl = www + "app/"
    public static let phoneVerifyUrl = rootUrl + "phoneVerify.php"
    public static let verifyCodeUrl = rootUrl + "verifyCode.php"
    public static let signUpUrl = rootUrl + "signUp.php"
    public static let changePhotoUrl = rootUrl + "changePhoto.php"
    public static let submitLicenceUrl = rootUrl + "submitLicence.php"
    public static let submitCarUrl = rootUrl + "submitCar.php"
    public static let submitBankUrl = rootUrl + "submitBank.php"
    public static let submitIDUrl = rootUrl + "submitID.php"
    public static let signInUrl = rootUrl + "signIn.php"
    public static let sendMailUrl = rootUrl + "sendMail.php"
    public static let actionsUrl = rootUrl + "actions.php"
    public static let getRoutesUrl = rootUrl + "getRoutes.php"
    public static let getRouteUrl = rootUrl + "getRoute.php"
    public static let searchRoutesUrl = rootUrl + "searchRoutes.php"
    public static let profileUrl = rootUrl + "profile.php"
    public static let editorUrl = rootUrl + "editor.php"
    public static let saveInfoUrl = rootUrl + "saveInfo.php"
    public static let stripeAPIUrl = rootUrl + "stripeAPI.php"
    public static let followRouteUrl = rootUrl + "followRoute.php"
    public static let routeUrl = rootUrl + "route.php"
    public static let notifierUrl = rootUrl + "notifier.php"
    public static let showRoutersUrl = rootUrl + "showRouters.php"
    public static let getDataUrl = rootUrl + "getData.php"
    public static let transactionsUrl = rootUrl + "transactions.php"
}
