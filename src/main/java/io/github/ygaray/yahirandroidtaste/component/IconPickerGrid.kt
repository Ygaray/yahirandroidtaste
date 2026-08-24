package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccessAlarms
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AccessibleForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.AddHomeWork
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AddToDrive
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.Addchart
import androidx.compose.material.icons.filled.AdfScanner
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AirlineSeatFlat
import androidx.compose.material.icons.filled.AirlineSeatFlatAngled
import androidx.compose.material.icons.filled.AirlineSeatIndividualSuite
import androidx.compose.material.icons.filled.AirlineSeatLegroomExtra
import androidx.compose.material.icons.filled.AirlineSeatLegroomNormal
import androidx.compose.material.icons.filled.AirlineSeatLegroomReduced
import androidx.compose.material.icons.filled.AirlineSeatReclineExtra
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.AirlineStops
import androidx.compose.material.icons.filled.Airlines
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirplanemodeInactive
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignHorizontalLeft
import androidx.compose.material.icons.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AllOut
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AmpStories
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Aod
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AppsOutage
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AreaChart
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDownCircle
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.AssignmentReturned
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AssistWalker
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.AssistantDirection
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.AssuredWorkload
import androidx.compose.material.icons.filled.Atm
import androidx.compose.material.icons.filled.AttachEmail
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Attribution
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.AutoFixOff
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.AutofpsSelect
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BackupTable
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Balcony
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BatchPrediction
import androidx.compose.material.icons.filled.Bathroom
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.BatteryUnknown
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.BedroomBaby
import androidx.compose.material.icons.filled.BedroomChild
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BedtimeOff
import androidx.compose.material.icons.filled.Beenhere
import androidx.compose.material.icons.filled.Bento
import androidx.compose.material.icons.filled.BikeScooter
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Blind
import androidx.compose.material.icons.filled.Blinds
import androidx.compose.material.icons.filled.BlindsClosed
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothDrive
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.BlurLinear
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.BorderBottom
import androidx.compose.material.icons.filled.BorderClear
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.BorderHorizontal
import androidx.compose.material.icons.filled.BorderInner
import androidx.compose.material.icons.filled.BorderLeft
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.BorderRight
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.BorderTop
import androidx.compose.material.icons.filled.BorderVertical
import androidx.compose.material.icons.filled.Boy
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.BroadcastOnHome
import androidx.compose.material.icons.filled.BroadcastOnPersonal
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.BrowserNotSupported
import androidx.compose.material.icons.filled.BrowserUpdated
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Bungalow
import androidx.compose.material.icons.filled.BurstMode
import androidx.compose.material.icons.filled.BusAlert
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallMissedOutgoing
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CallToAction
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraIndoor
import androidx.compose.material.icons.filled.CameraOutdoor
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.CameraRoll
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CancelPresentation
import androidx.compose.material.icons.filled.CancelScheduleSend
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.Cases
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CellWifi
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.ChairAlt
import androidx.compose.material.icons.filled.Chalet
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.ChargingStation
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.CodeOff
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CoffeeMaker
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.CommentBank
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Compost
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ConnectWithoutContact
import androidx.compose.material.icons.filled.ConnectedTv
import androidx.compose.material.icons.filled.ConnectingAirports
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContactEmergency
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.ContentPasteOff
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.ControlPoint
import androidx.compose.material.icons.filled.ControlPointDuplicate
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Countertops
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.Crib
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Crop169
import androidx.compose.material.icons.filled.Crop32
import androidx.compose.material.icons.filled.Crop54
import androidx.compose.material.icons.filled.Crop75
import androidx.compose.material.icons.filled.CropDin
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.Css
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyLira
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.CurrencyRuble
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.CurrencyYuan
import androidx.compose.material.icons.filled.Curtains
import androidx.compose.material.icons.filled.CurtainsClosed
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DataSaverOff
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DatasetLinked
import androidx.compose.material.icons.filled.Deblur
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DensityLarge
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material.icons.filled.DepartureBoard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Desk
import androidx.compose.material.icons.filled.DesktopAccessDisabled
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DeveloperBoardOff
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DevicesFold
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.DialerSip
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBoatFilled
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsBusFilled
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.DirectionsOff
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.DirectionsRailwayFilled
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DirectionsSubwayFilled
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.DirectionsTransitFilled
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirtyLens
import androidx.compose.material.icons.filled.DisabledByDefault
import androidx.compose.material.icons.filled.DisabledVisible
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Diversity2
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DoDisturb
import androidx.compose.material.icons.filled.DoDisturbAlt
import androidx.compose.material.icons.filled.DoDisturbOff
import androidx.compose.material.icons.filled.DoDisturbOn
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.DoNotDisturbAlt
import androidx.compose.material.icons.filled.DoNotDisturbOff
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.DoNotDisturbOnTotalSilence
import androidx.compose.material.icons.filled.DoNotStep
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.DomainAdd
import androidx.compose.material.icons.filled.DomainDisabled
import androidx.compose.material.icons.filled.DomainVerification
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.DonutSmall
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.DoorSliding
import androidx.compose.material.icons.filled.Doorbell
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileMoveRtl
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Dry
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Duo
import androidx.compose.material.icons.filled.Dvr
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.EMobiledata
import androidx.compose.material.icons.filled.Earbuds
import androidx.compose.material.icons.filled.EarbudsBattery
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EdgesensorHigh
import androidx.compose.material.icons.filled.EdgesensorLow
import androidx.compose.material.icons.filled.EditAttributes
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EditNotifications
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.EditRoad
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.Eject
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.ElderlyWoman
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EmergencyRecording
import androidx.compose.material.icons.filled.EmergencyShare
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EmojiFlags
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.EmojiSymbols
import androidx.compose.material.icons.filled.EmojiTransportation
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Escalator
import androidx.compose.material.icons.filled.EscalatorWarning
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.ExposureNeg1
import androidx.compose.material.icons.filled.ExposureNeg2
import androidx.compose.material.icons.filled.ExposurePlus1
import androidx.compose.material.icons.filled.ExposurePlus2
import androidx.compose.material.icons.filled.ExposureZero
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.ExtensionOff
import androidx.compose.material.icons.filled.Face2
import androidx.compose.material.icons.filled.Face3
import androidx.compose.material.icons.filled.Face4
import androidx.compose.material.icons.filled.Face5
import androidx.compose.material.icons.filled.Face6
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.FaceRetouchingOff
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Fax
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.FeaturedVideo
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.FiberDvr
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.FiberPin
import androidx.compose.material.icons.filled.FiberSmartRecord
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.Filter2
import androidx.compose.material.icons.filled.Filter3
import androidx.compose.material.icons.filled.Filter4
import androidx.compose.material.icons.filled.Filter5
import androidx.compose.material.icons.filled.Filter6
import androidx.compose.material.icons.filled.Filter7
import androidx.compose.material.icons.filled.Filter8
import androidx.compose.material.icons.filled.Filter9
import androidx.compose.material.icons.filled.Filter9Plus
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.FilterFrames
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.FilterTiltShift
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FireExtinguisher
import androidx.compose.material.icons.filled.FireHydrantAlt
import androidx.compose.material.icons.filled.FireTruck
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fitbit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material.icons.filled.Flaky
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.FlightClass
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.FlipCameraIos
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.Flourescent
import androidx.compose.material.icons.filled.Fluorescent
import androidx.compose.material.icons.filled.FlutterDash
import androidx.compose.material.icons.filled.FmdBad
import androidx.compose.material.icons.filled.FmdGood
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FollowTheSigns
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FontDownloadOff
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.ForkLeft
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatListNumberedRtl
import androidx.compose.material.icons.filled.FormatOverline
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatTextdirectionLToR
import androidx.compose.material.icons.filled.FormatTextdirectionRToL
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Fort
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.FreeCancellation
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GMobiledata
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GeneratingTokens
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.Girl
import androidx.compose.material.icons.filled.Gite
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.GridGoldenratio
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Groups3
import androidx.compose.material.icons.filled.HMobiledata
import androidx.compose.material.icons.filled.HPlusMobiledata
import androidx.compose.material.icons.filled.Hail
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.HdrAutoSelect
import androidx.compose.material.icons.filled.HdrEnhancedSelect
import androidx.compose.material.icons.filled.HdrOff
import androidx.compose.material.icons.filled.HdrOffSelect
import androidx.compose.material.icons.filled.HdrOn
import androidx.compose.material.icons.filled.HdrOnSelect
import androidx.compose.material.icons.filled.HdrPlus
import androidx.compose.material.icons.filled.HdrStrong
import androidx.compose.material.icons.filled.HdrWeak
import androidx.compose.material.icons.filled.HeadphonesBattery
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.HeatPump
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hevc
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HighlightAlt
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Hls
import androidx.compose.material.icons.filled.HlsOff
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.HomeMax
import androidx.compose.material.icons.filled.HomeMini
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.HorizontalSplit
import androidx.compose.material.icons.filled.HotTub
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.HotelClass
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassDisabled
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.HouseSiding
import androidx.compose.material.icons.filled.Houseboat
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Https
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Hvac
import androidx.compose.material.icons.filled.IceSkating
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.ImagesearchRoller
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.ImportantDevices
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material.icons.filled.InsertComment
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.InsertInvitation
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material.icons.filled.InsertPageBreak
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.InstallDesktop
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.InterpreterMode
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.InvertColorsOff
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.Iso
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.JoinFull
import androidx.compose.material.icons.filled.JoinInner
import androidx.compose.material.icons.filled.JoinLeft
import androidx.compose.material.icons.filled.JoinRight
import androidx.compose.material.icons.filled.Kayaking
import androidx.compose.material.icons.filled.KebabDining
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.KeyboardCommandKey
import androidx.compose.material.icons.filled.KeyboardControlKey
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.KeyboardOptionKey
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.KeyboardTab
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.KingBed
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Kitesurfing
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LabelImportant
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Landslide
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LaptopChromebook
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LaptopWindows
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LeakAdd
import androidx.compose.material.icons.filled.LeakRemove
import androidx.compose.material.icons.filled.LeaveBagsAtHome
import androidx.compose.material.icons.filled.LegendToggle
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.LensBlur
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.LibraryAddCheck
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.LineStyle
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LinkedCamera
import androidx.compose.material.icons.filled.Liquor
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LiveHelp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Living
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalAirport
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material.icons.filled.LocalConvenienceStore
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalHotel
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocalPlay
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.LocalSee
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Looks
import androidx.compose.material.icons.filled.Looks3
import androidx.compose.material.icons.filled.Looks4
import androidx.compose.material.icons.filled.Looks5
import androidx.compose.material.icons.filled.Looks6
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Loupe
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.LteMobiledata
import androidx.compose.material.icons.filled.LtePlusMobiledata
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MacroOff
import androidx.compose.material.icons.filled.MailLock
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Man2
import androidx.compose.material.icons.filled.Man3
import androidx.compose.material.icons.filled.Man4
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.ManageHistory
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MapsHomeWork
import androidx.compose.material.icons.filled.MapsUgc
import androidx.compose.material.icons.filled.Margin
import androidx.compose.material.icons.filled.MarkAsUnread
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.MarkUnreadChatAlt
import androidx.compose.material.icons.filled.Markunread
import androidx.compose.material.icons.filled.MarkunreadMailbox
import androidx.compose.material.icons.filled.Masks
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.MediaBluetoothOff
import androidx.compose.material.icons.filled.MediaBluetoothOn
import androidx.compose.material.icons.filled.Mediation
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicExternalOff
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Microwave
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.MinorCrash
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.MissedVideoCall
import androidx.compose.material.icons.filled.Mms
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.MobileOff
import androidx.compose.material.icons.filled.MobileScreenShare
import androidx.compose.material.icons.filled.MobiledataOff
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.ModeEditOutline
import androidx.compose.material.icons.filled.ModeFanOff
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.ModeOfTravel
import androidx.compose.material.icons.filled.ModeStandby
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.MoneyOffCsred
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MonochromePhotos
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.More
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.MotionPhotosPause
import androidx.compose.material.icons.filled.MotionPhotosPaused
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Moving
import androidx.compose.material.icons.filled.Mp
import androidx.compose.material.icons.filled.MultilineChart
import androidx.compose.material.icons.filled.MultipleStop
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Nat
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NearMeDisabled
import androidx.compose.material.icons.filled.NearbyError
import androidx.compose.material.icons.filled.NearbyOff
import androidx.compose.material.icons.filled.NestCamWiredStand
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NetworkLocked
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.NetworkWifi1Bar
import androidx.compose.material.icons.filled.NetworkWifi2Bar
import androidx.compose.material.icons.filled.NetworkWifi3Bar
import androidx.compose.material.icons.filled.NewLabel
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NextPlan
import androidx.compose.material.icons.filled.NextWeek
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material.icons.filled.NoBackpack
import androidx.compose.material.icons.filled.NoCell
import androidx.compose.material.icons.filled.NoCrash
import androidx.compose.material.icons.filled.NoDrinks
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.NoEncryptionGmailerrorred
import androidx.compose.material.icons.filled.NoFlash
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.NoLuggage
import androidx.compose.material.icons.filled.NoMeals
import androidx.compose.material.icons.filled.NoMeetingRoom
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.NoSim
import androidx.compose.material.icons.filled.NoStroller
import androidx.compose.material.icons.filled.NoTransfer
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.NoiseControlOff
import androidx.compose.material.icons.filled.NordicWalking
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.NotAccessible
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.NotListedLocation
import androidx.compose.material.icons.filled.NotStarted
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.OfflineShare
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.OnDeviceTraining
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.OpenInNewOff
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.OtherHouses
import androidx.compose.material.icons.filled.Outbond
import androidx.compose.material.icons.filled.Outbound
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Outlet
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material.icons.filled.Pageview
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PanToolAlt
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.PanoramaHorizontal
import androidx.compose.material.icons.filled.PanoramaHorizontalSelect
import androidx.compose.material.icons.filled.PanoramaPhotosphere
import androidx.compose.material.icons.filled.PanoramaPhotosphereSelect
import androidx.compose.material.icons.filled.PanoramaVertical
import androidx.compose.material.icons.filled.PanoramaVerticalSelect
import androidx.compose.material.icons.filled.PanoramaWideAngle
import androidx.compose.material.icons.filled.PanoramaWideAngleSelect
import androidx.compose.material.icons.filled.Paragliding
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PartyMode
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PausePresentation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Pentagon
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PermCameraMic
import androidx.compose.material.icons.filled.PermContactCalendar
import androidx.compose.material.icons.filled.PermDataSetting
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.PermIdentity
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PermPhoneMsg
import androidx.compose.material.icons.filled.PermScanWifi
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.Person3
import androidx.compose.material.icons.filled.Person4
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PersonAddDisabled
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PersonRemoveAlt1
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PersonalInjury
import androidx.compose.material.icons.filled.PersonalVideo
import androidx.compose.material.icons.filled.PestControl
import androidx.compose.material.icons.filled.PestControlRodent
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneBluetoothSpeaker
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.PhoneEnabled
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PhoneLocked
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.PhonePaused
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.PhonelinkOff
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoCameraBack
import androidx.compose.material.icons.filled.PhotoCameraFront
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.material.icons.filled.PhotoSizeSelectActual
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.PhotoSizeSelectSmall
import androidx.compose.material.icons.filled.Php
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PianoOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PieChartOutline
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PinEnd
import androidx.compose.material.icons.filled.PinInvoke
import androidx.compose.material.icons.filled.Pinch
import androidx.compose.material.icons.filled.PivotTableChart
import androidx.compose.material.icons.filled.Pix
import androidx.compose.material.icons.filled.Plagiarism
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.PlayDisabled
import androidx.compose.material.icons.filled.PlayForWork
import androidx.compose.material.icons.filled.PlayLesson
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.PlaylistAddCheckCircle
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Polymer
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.PortableWifiOff
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerInput
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.PrintDisabled
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.PrivateConnectivity
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material.icons.filled.PropaneTank
import androidx.compose.material.icons.filled.PsychologyAlt
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Quickreply
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RMobiledata
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RailwayAlert
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.RampLeft
import androidx.compose.material.icons.filled.RampRight
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.RawOff
import androidx.compose.material.icons.filled.RawOn
import androidx.compose.material.icons.filled.ReadMore
import androidx.compose.material.icons.filled.RealEstateAgent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RecentActors
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.ReduceCapacity
import androidx.compose.material.icons.filled.RememberMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.RemoveFromQueue
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.RemoveRoad
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Repartition
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Replay30
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.ReplayCircleFilled
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.ReplyAll
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ReportGmailerrorred
import androidx.compose.material.icons.filled.ReportOff
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.RequestPage
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.ResetTv
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.RollerShades
import androidx.compose.material.icons.filled.RollerShadesClosed
import androidx.compose.material.icons.filled.RollerSkating
import androidx.compose.material.icons.filled.Roofing
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.RoomPreferences
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.RoundaboutLeft
import androidx.compose.material.icons.filled.RoundaboutRight
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Rowing
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Rsvp
import androidx.compose.material.icons.filled.Rtt
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.RuleFolder
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material.icons.filled.RunningWithErrors
import androidx.compose.material.icons.filled.RvHookup
import androidx.compose.material.icons.filled.SafetyCheck
import androidx.compose.material.icons.filled.SafetyDivider
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.ScatterPlot
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScheduleSend
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material.icons.filled.Score
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.ScreenLockLandscape
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenRotationAlt
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material.icons.filled.ScubaDiving
import androidx.compose.material.icons.filled.Sd
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SecurityUpdate
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.filled.SecurityUpdateWarning
import androidx.compose.material.icons.filled.Segment
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SendAndArchive
import androidx.compose.material.icons.filled.SendTimeExtension
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.material.icons.filled.SensorDoor
import androidx.compose.material.icons.filled.SensorOccupied
import androidx.compose.material.icons.filled.SensorWindow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.SettingsBluetooth
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.SettingsInputComposite
import androidx.compose.material.icons.filled.SettingsInputHdmi
import androidx.compose.material.icons.filled.SettingsInputSvideo
import androidx.compose.material.icons.filled.SettingsOverscan
import androidx.compose.material.icons.filled.SettingsPhone
import androidx.compose.material.icons.filled.SettingsPower
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.SevereCold
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Shop2
import androidx.compose.material.icons.filled.ShopTwo
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Shortcut
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.ShutterSpeed
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material.icons.filled.SignalCellular0Bar
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularAlt1Bar
import androidx.compose.material.icons.filled.SignalCellularAlt2Bar
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material.icons.filled.SignalCellularNoSim
import androidx.compose.material.icons.filled.SignalCellularNodata
import androidx.compose.material.icons.filled.SignalCellularNull
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.SignalWifi0Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifi4BarLock
import androidx.compose.material.icons.filled.SignalWifiBad
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SignalWifiStatusbar4Bar
import androidx.compose.material.icons.filled.SignalWifiStatusbarConnectedNoInternet4
import androidx.compose.material.icons.filled.SignalWifiStatusbarNull
import androidx.compose.material.icons.filled.Signpost
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SimCardAlert
import androidx.compose.material.icons.filled.SimCardDownload
import androidx.compose.material.icons.filled.SingleBed
import androidx.compose.material.icons.filled.Sip
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sledding
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.SmartScreen
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SmokeFree
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SmsFailed
import androidx.compose.material.icons.filled.SnippetFolder
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Snowboarding
import androidx.compose.material.icons.filled.Snowmobile
import androidx.compose.material.icons.filled.Snowshoeing
import androidx.compose.material.icons.filled.Soap
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.SouthAmerica
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material.icons.filled.SpatialTracking
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material.icons.filled.SpeakerPhone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Spoke
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsHockey
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.SportsRugby
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarBorderPurple500
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.StarPurple500
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.StayPrimaryLandscape
import androidx.compose.material.icons.filled.StayPrimaryPortrait
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.StoreMallDirectory
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Storm
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.Streetview
import androidx.compose.material.icons.filled.StrikethroughS
import androidx.compose.material.icons.filled.Stroller
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SubdirectoryArrowLeft
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Subscript
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Superscript
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapHorizontalCircle
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material.icons.filled.SwipeDownAlt
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeLeftAlt
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.SwipeRightAlt
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material.icons.filled.SwipeUpAlt
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.SwitchAccessShortcut
import androidx.compose.material.icons.filled.SwitchAccessShortcutAdd
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material.icons.filled.SwitchLeft
import androidx.compose.material.icons.filled.SwitchRight
import androidx.compose.material.icons.filled.SwitchVideo
import androidx.compose.material.icons.filled.Synagogue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.SystemSecurityUpdate
import androidx.compose.material.icons.filled.SystemSecurityUpdateGood
import androidx.compose.material.icons.filled.SystemSecurityUpdateWarning
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.TabUnselected
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.material.icons.filled.TakeoutDining
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.material.icons.filled.Tapas
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TaxiAlert
import androidx.compose.material.icons.filled.TempleBuddhist
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.TextRotateUp
import androidx.compose.material.icons.filled.TextRotateVertical
import androidx.compose.material.icons.filled.TextRotationAngledown
import androidx.compose.material.icons.filled.TextRotationAngleup
import androidx.compose.material.icons.filled.TextRotationDown
import androidx.compose.material.icons.filled.TextRotationNone
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ThermostatAuto
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownAlt
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.filled.Timer10Select
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material.icons.filled.Timer3Select
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Toc
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material.icons.filled.Tornado
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material.icons.filled.Transcribe
import androidx.compose.material.icons.filled.TransferWithinAStation
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material.icons.filled.TransitEnterexit
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material.icons.filled.Tsunami
import androidx.compose.material.icons.filled.Tty
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tungsten
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TurnSharpLeft
import androidx.compose.material.icons.filled.TurnSharpRight
import androidx.compose.material.icons.filled.TurnSlightLeft
import androidx.compose.material.icons.filled.TurnSlightRight
import androidx.compose.material.icons.filled.TurnedIn
import androidx.compose.material.icons.filled.TurnedInNot
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.TvOff
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.TypeSpecimen
import androidx.compose.material.icons.filled.UTurnLeft
import androidx.compose.material.icons.filled.UTurnRight
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldLessDouble
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.UnfoldMoreDouble
import androidx.compose.material.icons.filled.Unpublished
import androidx.compose.material.icons.filled.Unsubscribe
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.VapeFree
import androidx.compose.material.icons.filled.VapingRooms
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.VerticalDistribute
import androidx.compose.material.icons.filled.VerticalShades
import androidx.compose.material.icons.filled.VerticalShadesClosed
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.VideoChat
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material.icons.filled.VideoStable
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.VideogameAssetOff
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewComfy
import androidx.compose.material.icons.filled.ViewComfyAlt
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.ViewCompactAlt
import androidx.compose.material.icons.filled.ViewCozy
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Vignette
import androidx.compose.material.icons.filled.Villa
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.filled.Volcano
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VpnKeyOff
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Vrpano
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wash
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.filled.WatchOff
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WaterfallChart
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.WbAuto
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material.icons.filled.WbIridescent
import androidx.compose.material.icons.filled.WbShade
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material.icons.filled.WebAssetOff
import androidx.compose.material.icons.filled.WebStories
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.West
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WheelchairPickup
import androidx.compose.material.icons.filled.WhereToVote
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material.icons.filled.WidthNormal
import androidx.compose.material.icons.filled.WidthWide
import androidx.compose.material.icons.filled.Wifi1Bar
import androidx.compose.material.icons.filled.Wifi2Bar
import androidx.compose.material.icons.filled.WifiCalling
import androidx.compose.material.icons.filled.WifiCalling3
import androidx.compose.material.icons.filled.WifiChannel
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material.icons.filled.WifiLock
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.WifiPassword
import androidx.compose.material.icons.filled.WifiProtectedSetup
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.WifiTetheringError
import androidx.compose.material.icons.filled.WifiTetheringErrorRounded
import androidx.compose.material.icons.filled.WifiTetheringOff
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material.icons.filled.Woman2
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material.icons.filled.Wysiwyg
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

val ICON_MAP: Map<String, ImageVector> = mapOf(
    "book" to Icons.Default.Book,
    "star" to Icons.Default.Star,
    "work" to Icons.Default.Work,
    "school" to Icons.Default.School,
    "home" to Icons.Default.Home,
    "favorite" to Icons.Default.Favorite,
    "music" to Icons.Default.MusicNote,
    "camera" to Icons.Default.CameraAlt,
    "palette" to Icons.Default.Palette,
    "fitness" to Icons.Default.FitnessCenter,
    "restaurant" to Icons.Default.Restaurant,
    "flight" to Icons.Default.Flight,
    "code" to Icons.Default.Code,
    "science" to Icons.Default.Science,
    "pets" to Icons.Default.Pets,
    "nature" to Icons.Default.Nature,
    "shopping" to Icons.Default.ShoppingCart,
    "medical" to Icons.Default.MedicalServices,
    "build" to Icons.Default.Build,
    "movie" to Icons.Default.Movie,
    "sports" to Icons.Default.SportsBasketball,
    "games" to Icons.Default.SportsEsports,
    "photo" to Icons.Default.PhotoLibrary,
    "mail" to Icons.Default.Email,
    "phone" to Icons.Default.Phone,
    "money" to Icons.Default.AttachMoney,
    "lightbulb" to Icons.Default.Lightbulb,
    "directions_car" to Icons.Default.DirectionsCar,
    "local_cafe" to Icons.Default.LocalCafe,
    "psychology" to Icons.Default.Psychology,
    "checklist" to Icons.Default.Checklist,
    "timer" to Icons.Default.Timer,
    "alarm" to Icons.Default.Alarm,
    "calendar" to Icons.Default.CalendarMonth,
    "map" to Icons.Default.Map,
    "language" to Icons.Default.Language,
    "brush" to Icons.Default.Brush,
    "headphones" to Icons.Default.Headphones,
    "wifi" to Icons.Default.Wifi,
    "bolt" to Icons.Default.Bolt,
    "rocket" to Icons.Default.RocketLaunch,
    "group" to Icons.Default.Group,
    "child" to Icons.Default.ChildCare,
    "cake" to Icons.Default.Cake,
    "sunny" to Icons.Default.WbSunny,
    "nightlight" to Icons.Default.NightsStay,
    "forest" to Icons.Default.Forest,
    "beach" to Icons.Default.BeachAccess,
    "shield" to Icons.Default.Shield,
    "lock" to Icons.Default.Lock,
    // --- Codegen-appended Filled icon set (Plan 66-03, D-02/D-03) ---
    // Generated once from the resolved material-icons-extended-android-1.7.8.aar
    // (androidx/compose/material/icons/filled/*Kt.class facade enumeration). Append-only:
    // never edit or reorder the 50 entries above this line (frozen tags.icon_name keys).
    "abc" to Icons.Filled.Abc,
    "access_alarm" to Icons.Filled.AccessAlarm,
    "access_alarms" to Icons.Filled.AccessAlarms,
    "accessibility" to Icons.Filled.Accessibility,
    "accessibility_new" to Icons.Filled.AccessibilityNew,
    "accessible" to Icons.Filled.Accessible,
    "accessible_forward" to Icons.Filled.AccessibleForward,
    "access_time" to Icons.Filled.AccessTime,
    "access_time_filled" to Icons.Filled.AccessTimeFilled,
    "account_balance" to Icons.Filled.AccountBalance,
    "account_balance_wallet" to Icons.Filled.AccountBalanceWallet,
    "account_tree" to Icons.Filled.AccountTree,
    "ac_unit" to Icons.Filled.AcUnit,
    "adb" to Icons.Filled.Adb,
    "add_alarm" to Icons.Filled.AddAlarm,
    "add_alert" to Icons.Filled.AddAlert,
    "add_a_photo" to Icons.Filled.AddAPhoto,
    "add_box" to Icons.Filled.AddBox,
    "add_business" to Icons.Filled.AddBusiness,
    "add_card" to Icons.Filled.AddCard,
    "addchart" to Icons.Filled.Addchart,
    "add_chart" to Icons.Filled.AddChart,
    "add_circle_outline" to Icons.Filled.AddCircleOutline,
    "add_comment" to Icons.Filled.AddComment,
    "add_home" to Icons.Filled.AddHome,
    "add_home_work" to Icons.Filled.AddHomeWork,
    "add_ic_call" to Icons.Filled.AddIcCall,
    "add_link" to Icons.Filled.AddLink,
    "add_location" to Icons.Filled.AddLocation,
    "add_location_alt" to Icons.Filled.AddLocationAlt,
    "add_moderator" to Icons.Filled.AddModerator,
    "add_photo_alternate" to Icons.Filled.AddPhotoAlternate,
    "add_reaction" to Icons.Filled.AddReaction,
    "add_road" to Icons.Filled.AddRoad,
    "add_shopping_cart" to Icons.Filled.AddShoppingCart,
    "add_task" to Icons.Filled.AddTask,
    "add_to_drive" to Icons.Filled.AddToDrive,
    "add_to_home_screen" to Icons.Filled.AddToHomeScreen,
    "add_to_photos" to Icons.Filled.AddToPhotos,
    "add_to_queue" to Icons.Filled.AddToQueue,
    "adf_scanner" to Icons.Filled.AdfScanner,
    "adjust" to Icons.Filled.Adjust,
    "admin_panel_settings" to Icons.Filled.AdminPanelSettings,
    "ads_click" to Icons.Filled.AdsClick,
    "ad_units" to Icons.Filled.AdUnits,
    "agriculture" to Icons.Filled.Agriculture,
    "air" to Icons.Filled.Air,
    "airlines" to Icons.Filled.Airlines,
    "airline_seat_flat" to Icons.Filled.AirlineSeatFlat,
    "airline_seat_flat_angled" to Icons.Filled.AirlineSeatFlatAngled,
    "airline_seat_individual_suite" to Icons.Filled.AirlineSeatIndividualSuite,
    "airline_seat_legroom_extra" to Icons.Filled.AirlineSeatLegroomExtra,
    "airline_seat_legroom_normal" to Icons.Filled.AirlineSeatLegroomNormal,
    "airline_seat_legroom_reduced" to Icons.Filled.AirlineSeatLegroomReduced,
    "airline_seat_recline_extra" to Icons.Filled.AirlineSeatReclineExtra,
    "airline_seat_recline_normal" to Icons.Filled.AirlineSeatReclineNormal,
    "airline_stops" to Icons.Filled.AirlineStops,
    "airplanemode_active" to Icons.Filled.AirplanemodeActive,
    "airplanemode_inactive" to Icons.Filled.AirplanemodeInactive,
    "airplane_ticket" to Icons.Filled.AirplaneTicket,
    "airplay" to Icons.Filled.Airplay,
    "airport_shuttle" to Icons.Filled.AirportShuttle,
    "alarm_add" to Icons.Filled.AlarmAdd,
    "alarm_off" to Icons.Filled.AlarmOff,
    "alarm_on" to Icons.Filled.AlarmOn,
    "album" to Icons.Filled.Album,
    "align_horizontal_center" to Icons.Filled.AlignHorizontalCenter,
    "align_horizontal_left" to Icons.Filled.AlignHorizontalLeft,
    "align_horizontal_right" to Icons.Filled.AlignHorizontalRight,
    "align_vertical_bottom" to Icons.Filled.AlignVerticalBottom,
    "align_vertical_center" to Icons.Filled.AlignVerticalCenter,
    "align_vertical_top" to Icons.Filled.AlignVerticalTop,
    "all_inbox" to Icons.Filled.AllInbox,
    "all_inclusive" to Icons.Filled.AllInclusive,
    "all_out" to Icons.Filled.AllOut,
    "alternate_email" to Icons.Filled.AlternateEmail,
    "alt_route" to Icons.Filled.AltRoute,
    "amp_stories" to Icons.Filled.AmpStories,
    "analytics" to Icons.Filled.Analytics,
    "anchor" to Icons.Filled.Anchor,
    "android" to Icons.Filled.Android,
    "animation" to Icons.Filled.Animation,
    "announcement" to Icons.Filled.Announcement,
    "aod" to Icons.Filled.Aod,
    "apartment" to Icons.Filled.Apartment,
    "api" to Icons.Filled.Api,
    "app_blocking" to Icons.Filled.AppBlocking,
    "app_registration" to Icons.Filled.AppRegistration,
    "approval" to Icons.Filled.Approval,
    "apps" to Icons.Filled.Apps,
    "app_settings_alt" to Icons.Filled.AppSettingsAlt,
    "app_shortcut" to Icons.Filled.AppShortcut,
    "apps_outage" to Icons.Filled.AppsOutage,
    "architecture" to Icons.Filled.Architecture,
    "archive" to Icons.Filled.Archive,
    "area_chart" to Icons.Filled.AreaChart,
    "arrow_back_ios" to Icons.Filled.ArrowBackIos,
    "arrow_back_ios_new" to Icons.Filled.ArrowBackIosNew,
    "arrow_circle_down" to Icons.Filled.ArrowCircleDown,
    "arrow_circle_left" to Icons.Filled.ArrowCircleLeft,
    "arrow_circle_right" to Icons.Filled.ArrowCircleRight,
    "arrow_circle_up" to Icons.Filled.ArrowCircleUp,
    "arrow_downward" to Icons.Filled.ArrowDownward,
    "arrow_drop_down_circle" to Icons.Filled.ArrowDropDownCircle,
    "arrow_drop_up" to Icons.Filled.ArrowDropUp,
    "arrow_forward_ios" to Icons.Filled.ArrowForwardIos,
    "arrow_left" to Icons.Filled.ArrowLeft,
    "arrow_outward" to Icons.Filled.ArrowOutward,
    "arrow_right" to Icons.Filled.ArrowRight,
    "arrow_right_alt" to Icons.Filled.ArrowRightAlt,
    "arrow_upward" to Icons.Filled.ArrowUpward,
    "article" to Icons.Filled.Article,
    "art_track" to Icons.Filled.ArtTrack,
    "aspect_ratio" to Icons.Filled.AspectRatio,
    "assessment" to Icons.Filled.Assessment,
    "assignment" to Icons.Filled.Assignment,
    "assignment_ind" to Icons.Filled.AssignmentInd,
    "assignment_late" to Icons.Filled.AssignmentLate,
    "assignment_return" to Icons.Filled.AssignmentReturn,
    "assignment_returned" to Icons.Filled.AssignmentReturned,
    "assignment_turned_in" to Icons.Filled.AssignmentTurnedIn,
    "assistant" to Icons.Filled.Assistant,
    "assistant_direction" to Icons.Filled.AssistantDirection,
    "assistant_photo" to Icons.Filled.AssistantPhoto,
    "assist_walker" to Icons.Filled.AssistWalker,
    "assured_workload" to Icons.Filled.AssuredWorkload,
    "atm" to Icons.Filled.Atm,
    "attach_email" to Icons.Filled.AttachEmail,
    "attach_file" to Icons.Filled.AttachFile,
    "attachment" to Icons.Filled.Attachment,
    "attach_money" to Icons.Filled.AttachMoney,
    "attractions" to Icons.Filled.Attractions,
    "attribution" to Icons.Filled.Attribution,
    "audio_file" to Icons.Filled.AudioFile,
    "audiotrack" to Icons.Filled.Audiotrack,
    "auto_awesome" to Icons.Filled.AutoAwesome,
    "auto_awesome_mosaic" to Icons.Filled.AutoAwesomeMosaic,
    "auto_awesome_motion" to Icons.Filled.AutoAwesomeMotion,
    "auto_delete" to Icons.Filled.AutoDelete,
    "auto_fix_high" to Icons.Filled.AutoFixHigh,
    "auto_fix_normal" to Icons.Filled.AutoFixNormal,
    "auto_fix_off" to Icons.Filled.AutoFixOff,
    "autofps_select" to Icons.Filled.AutofpsSelect,
    "auto_graph" to Icons.Filled.AutoGraph,
    "auto_mode" to Icons.Filled.AutoMode,
    "autorenew" to Icons.Filled.Autorenew,
    "auto_stories" to Icons.Filled.AutoStories,
    "av_timer" to Icons.Filled.AvTimer,
    "baby_changing_station" to Icons.Filled.BabyChangingStation,
    "back_hand" to Icons.Filled.BackHand,
    "backpack" to Icons.Filled.Backpack,
    "backspace" to Icons.Filled.Backspace,
    "backup" to Icons.Filled.Backup,
    "backup_table" to Icons.Filled.BackupTable,
    "badge" to Icons.Filled.Badge,
    "bakery_dining" to Icons.Filled.BakeryDining,
    "balance" to Icons.Filled.Balance,
    "balcony" to Icons.Filled.Balcony,
    "ballot" to Icons.Filled.Ballot,
    "bar_chart" to Icons.Filled.BarChart,
    "batch_prediction" to Icons.Filled.BatchPrediction,
    "bathroom" to Icons.Filled.Bathroom,
    "bathtub" to Icons.Filled.Bathtub,
    "battery_0_bar" to Icons.Filled.Battery0Bar,
    "battery_1_bar" to Icons.Filled.Battery1Bar,
    "battery_2_bar" to Icons.Filled.Battery2Bar,
    "battery_3_bar" to Icons.Filled.Battery3Bar,
    "battery_4_bar" to Icons.Filled.Battery4Bar,
    "battery_5_bar" to Icons.Filled.Battery5Bar,
    "battery_6_bar" to Icons.Filled.Battery6Bar,
    "battery_alert" to Icons.Filled.BatteryAlert,
    "battery_charging_full" to Icons.Filled.BatteryChargingFull,
    "battery_full" to Icons.Filled.BatteryFull,
    "battery_saver" to Icons.Filled.BatterySaver,
    "battery_std" to Icons.Filled.BatteryStd,
    "battery_unknown" to Icons.Filled.BatteryUnknown,
    "beach_access" to Icons.Filled.BeachAccess,
    "bed" to Icons.Filled.Bed,
    "bedroom_baby" to Icons.Filled.BedroomBaby,
    "bedroom_child" to Icons.Filled.BedroomChild,
    "bedroom_parent" to Icons.Filled.BedroomParent,
    "bedtime" to Icons.Filled.Bedtime,
    "bedtime_off" to Icons.Filled.BedtimeOff,
    "beenhere" to Icons.Filled.Beenhere,
    "bento" to Icons.Filled.Bento,
    "bike_scooter" to Icons.Filled.BikeScooter,
    "biotech" to Icons.Filled.Biotech,
    "blender" to Icons.Filled.Blender,
    "blind" to Icons.Filled.Blind,
    "blinds" to Icons.Filled.Blinds,
    "blinds_closed" to Icons.Filled.BlindsClosed,
    "block" to Icons.Filled.Block,
    "bloodtype" to Icons.Filled.Bloodtype,
    "bluetooth" to Icons.Filled.Bluetooth,
    "bluetooth_audio" to Icons.Filled.BluetoothAudio,
    "bluetooth_connected" to Icons.Filled.BluetoothConnected,
    "bluetooth_disabled" to Icons.Filled.BluetoothDisabled,
    "bluetooth_drive" to Icons.Filled.BluetoothDrive,
    "bluetooth_searching" to Icons.Filled.BluetoothSearching,
    "blur_circular" to Icons.Filled.BlurCircular,
    "blur_linear" to Icons.Filled.BlurLinear,
    "blur_off" to Icons.Filled.BlurOff,
    "blur_on" to Icons.Filled.BlurOn,
    "bookmark" to Icons.Filled.Bookmark,
    "bookmark_add" to Icons.Filled.BookmarkAdd,
    "bookmark_added" to Icons.Filled.BookmarkAdded,
    "bookmark_border" to Icons.Filled.BookmarkBorder,
    "bookmark_remove" to Icons.Filled.BookmarkRemove,
    "bookmarks" to Icons.Filled.Bookmarks,
    "book_online" to Icons.Filled.BookOnline,
    "border_all" to Icons.Filled.BorderAll,
    "border_bottom" to Icons.Filled.BorderBottom,
    "border_clear" to Icons.Filled.BorderClear,
    "border_color" to Icons.Filled.BorderColor,
    "border_horizontal" to Icons.Filled.BorderHorizontal,
    "border_inner" to Icons.Filled.BorderInner,
    "border_left" to Icons.Filled.BorderLeft,
    "border_outer" to Icons.Filled.BorderOuter,
    "border_right" to Icons.Filled.BorderRight,
    "border_style" to Icons.Filled.BorderStyle,
    "border_top" to Icons.Filled.BorderTop,
    "border_vertical" to Icons.Filled.BorderVertical,
    "boy" to Icons.Filled.Boy,
    "branding_watermark" to Icons.Filled.BrandingWatermark,
    "breakfast_dining" to Icons.Filled.BreakfastDining,
    "brightness_1" to Icons.Filled.Brightness1,
    "brightness_2" to Icons.Filled.Brightness2,
    "brightness_3" to Icons.Filled.Brightness3,
    "brightness_4" to Icons.Filled.Brightness4,
    "brightness_5" to Icons.Filled.Brightness5,
    "brightness_6" to Icons.Filled.Brightness6,
    "brightness_7" to Icons.Filled.Brightness7,
    "brightness_auto" to Icons.Filled.BrightnessAuto,
    "brightness_high" to Icons.Filled.BrightnessHigh,
    "brightness_low" to Icons.Filled.BrightnessLow,
    "brightness_medium" to Icons.Filled.BrightnessMedium,
    "broadcast_on_home" to Icons.Filled.BroadcastOnHome,
    "broadcast_on_personal" to Icons.Filled.BroadcastOnPersonal,
    "broken_image" to Icons.Filled.BrokenImage,
    "browse_gallery" to Icons.Filled.BrowseGallery,
    "browser_not_supported" to Icons.Filled.BrowserNotSupported,
    "browser_updated" to Icons.Filled.BrowserUpdated,
    "brunch_dining" to Icons.Filled.BrunchDining,
    "bubble_chart" to Icons.Filled.BubbleChart,
    "bug_report" to Icons.Filled.BugReport,
    "build_circle" to Icons.Filled.BuildCircle,
    "bungalow" to Icons.Filled.Bungalow,
    "burst_mode" to Icons.Filled.BurstMode,
    "bus_alert" to Icons.Filled.BusAlert,
    "business" to Icons.Filled.Business,
    "business_center" to Icons.Filled.BusinessCenter,
    "cabin" to Icons.Filled.Cabin,
    "cable" to Icons.Filled.Cable,
    "cached" to Icons.Filled.Cached,
    "calculate" to Icons.Filled.Calculate,
    "calendar_month" to Icons.Filled.CalendarMonth,
    "calendar_today" to Icons.Filled.CalendarToday,
    "calendar_view_day" to Icons.Filled.CalendarViewDay,
    "calendar_view_month" to Icons.Filled.CalendarViewMonth,
    "calendar_view_week" to Icons.Filled.CalendarViewWeek,
    "call_end" to Icons.Filled.CallEnd,
    "call_made" to Icons.Filled.CallMade,
    "call_merge" to Icons.Filled.CallMerge,
    "call_missed" to Icons.Filled.CallMissed,
    "call_missed_outgoing" to Icons.Filled.CallMissedOutgoing,
    "call_received" to Icons.Filled.CallReceived,
    "call_split" to Icons.Filled.CallSplit,
    "call_to_action" to Icons.Filled.CallToAction,
    "camera_alt" to Icons.Filled.CameraAlt,
    "camera_enhance" to Icons.Filled.CameraEnhance,
    "camera_front" to Icons.Filled.CameraFront,
    "camera_indoor" to Icons.Filled.CameraIndoor,
    "camera_outdoor" to Icons.Filled.CameraOutdoor,
    "camera_rear" to Icons.Filled.CameraRear,
    "camera_roll" to Icons.Filled.CameraRoll,
    "cameraswitch" to Icons.Filled.Cameraswitch,
    "campaign" to Icons.Filled.Campaign,
    "cancel" to Icons.Filled.Cancel,
    "cancel_presentation" to Icons.Filled.CancelPresentation,
    "cancel_schedule_send" to Icons.Filled.CancelScheduleSend,
    "candlestick_chart" to Icons.Filled.CandlestickChart,
    "car_crash" to Icons.Filled.CarCrash,
    "card_giftcard" to Icons.Filled.CardGiftcard,
    "card_membership" to Icons.Filled.CardMembership,
    "card_travel" to Icons.Filled.CardTravel,
    "carpenter" to Icons.Filled.Carpenter,
    "car_rental" to Icons.Filled.CarRental,
    "car_repair" to Icons.Filled.CarRepair,
    "cases" to Icons.Filled.Cases,
    "casino" to Icons.Filled.Casino,
    "cast" to Icons.Filled.Cast,
    "cast_connected" to Icons.Filled.CastConnected,
    "cast_for_education" to Icons.Filled.CastForEducation,
    "castle" to Icons.Filled.Castle,
    "catching_pokemon" to Icons.Filled.CatchingPokemon,
    "category" to Icons.Filled.Category,
    "celebration" to Icons.Filled.Celebration,
    "cell_tower" to Icons.Filled.CellTower,
    "cell_wifi" to Icons.Filled.CellWifi,
    "center_focus_strong" to Icons.Filled.CenterFocusStrong,
    "center_focus_weak" to Icons.Filled.CenterFocusWeak,
    "chair" to Icons.Filled.Chair,
    "chair_alt" to Icons.Filled.ChairAlt,
    "chalet" to Icons.Filled.Chalet,
    "change_circle" to Icons.Filled.ChangeCircle,
    "change_history" to Icons.Filled.ChangeHistory,
    "charging_station" to Icons.Filled.ChargingStation,
    "chat" to Icons.Filled.Chat,
    "chat_bubble" to Icons.Filled.ChatBubble,
    "chat_bubble_outline" to Icons.Filled.ChatBubbleOutline,
    "check_box" to Icons.Filled.CheckBox,
    "check_box_outline_blank" to Icons.Filled.CheckBoxOutlineBlank,
    "check_circle_outline" to Icons.Filled.CheckCircleOutline,
    "checklist_rtl" to Icons.Filled.ChecklistRtl,
    "checkroom" to Icons.Filled.Checkroom,
    "chevron_left" to Icons.Filled.ChevronLeft,
    "chevron_right" to Icons.Filled.ChevronRight,
    "child_care" to Icons.Filled.ChildCare,
    "child_friendly" to Icons.Filled.ChildFriendly,
    "chrome_reader_mode" to Icons.Filled.ChromeReaderMode,
    "church" to Icons.Filled.Church,
    "circle" to Icons.Filled.Circle,
    "circle_notifications" to Icons.Filled.CircleNotifications,
    "clean_hands" to Icons.Filled.CleanHands,
    "cleaning_services" to Icons.Filled.CleaningServices,
    "clear_all" to Icons.Filled.ClearAll,
    "closed_caption" to Icons.Filled.ClosedCaption,
    "closed_caption_disabled" to Icons.Filled.ClosedCaptionDisabled,
    "closed_caption_off" to Icons.Filled.ClosedCaptionOff,
    "close_fullscreen" to Icons.Filled.CloseFullscreen,
    "cloud" to Icons.Filled.Cloud,
    "cloud_circle" to Icons.Filled.CloudCircle,
    "cloud_done" to Icons.Filled.CloudDone,
    "cloud_download" to Icons.Filled.CloudDownload,
    "cloud_off" to Icons.Filled.CloudOff,
    "cloud_queue" to Icons.Filled.CloudQueue,
    "cloud_sync" to Icons.Filled.CloudSync,
    "cloud_upload" to Icons.Filled.CloudUpload,
    "co_2" to Icons.Filled.Co2,
    "code_off" to Icons.Filled.CodeOff,
    "coffee" to Icons.Filled.Coffee,
    "coffee_maker" to Icons.Filled.CoffeeMaker,
    "collections" to Icons.Filled.Collections,
    "collections_bookmark" to Icons.Filled.CollectionsBookmark,
    "colorize" to Icons.Filled.Colorize,
    "color_lens" to Icons.Filled.ColorLens,
    "comment" to Icons.Filled.Comment,
    "comment_bank" to Icons.Filled.CommentBank,
    "comments_disabled" to Icons.Filled.CommentsDisabled,
    "commit" to Icons.Filled.Commit,
    "commute" to Icons.Filled.Commute,
    "compare" to Icons.Filled.Compare,
    "compare_arrows" to Icons.Filled.CompareArrows,
    "compass_calibration" to Icons.Filled.CompassCalibration,
    "compost" to Icons.Filled.Compost,
    "compress" to Icons.Filled.Compress,
    "computer" to Icons.Filled.Computer,
    "confirmation_number" to Icons.Filled.ConfirmationNumber,
    "connected_tv" to Icons.Filled.ConnectedTv,
    "connecting_airports" to Icons.Filled.ConnectingAirports,
    "connect_without_contact" to Icons.Filled.ConnectWithoutContact,
    "construction" to Icons.Filled.Construction,
    "contact_emergency" to Icons.Filled.ContactEmergency,
    "contactless" to Icons.Filled.Contactless,
    "contact_mail" to Icons.Filled.ContactMail,
    "contact_page" to Icons.Filled.ContactPage,
    "contact_phone" to Icons.Filled.ContactPhone,
    "contacts" to Icons.Filled.Contacts,
    "contact_support" to Icons.Filled.ContactSupport,
    "content_copy" to Icons.Filled.ContentCopy,
    "content_cut" to Icons.Filled.ContentCut,
    "content_paste" to Icons.Filled.ContentPaste,
    "content_paste_go" to Icons.Filled.ContentPasteGo,
    "content_paste_off" to Icons.Filled.ContentPasteOff,
    "content_paste_search" to Icons.Filled.ContentPasteSearch,
    "contrast" to Icons.Filled.Contrast,
    "control_camera" to Icons.Filled.ControlCamera,
    "control_point" to Icons.Filled.ControlPoint,
    "control_point_duplicate" to Icons.Filled.ControlPointDuplicate,
    "cookie" to Icons.Filled.Cookie,
    "co_present" to Icons.Filled.CoPresent,
    "copy_all" to Icons.Filled.CopyAll,
    "copyright" to Icons.Filled.Copyright,
    "coronavirus" to Icons.Filled.Coronavirus,
    "corporate_fare" to Icons.Filled.CorporateFare,
    "cottage" to Icons.Filled.Cottage,
    "countertops" to Icons.Filled.Countertops,
    "create_new_folder" to Icons.Filled.CreateNewFolder,
    "credit_card" to Icons.Filled.CreditCard,
    "credit_card_off" to Icons.Filled.CreditCardOff,
    "credit_score" to Icons.Filled.CreditScore,
    "crib" to Icons.Filled.Crib,
    "crisis_alert" to Icons.Filled.CrisisAlert,
    "crop" to Icons.Filled.Crop,
    "crop_169" to Icons.Filled.Crop169,
    "crop_32" to Icons.Filled.Crop32,
    "crop_54" to Icons.Filled.Crop54,
    "crop_75" to Icons.Filled.Crop75,
    "crop_din" to Icons.Filled.CropDin,
    "crop_free" to Icons.Filled.CropFree,
    "crop_landscape" to Icons.Filled.CropLandscape,
    "crop_original" to Icons.Filled.CropOriginal,
    "crop_portrait" to Icons.Filled.CropPortrait,
    "crop_rotate" to Icons.Filled.CropRotate,
    "crop_square" to Icons.Filled.CropSquare,
    "cruelty_free" to Icons.Filled.CrueltyFree,
    "css" to Icons.Filled.Css,
    "currency_bitcoin" to Icons.Filled.CurrencyBitcoin,
    "currency_exchange" to Icons.Filled.CurrencyExchange,
    "currency_franc" to Icons.Filled.CurrencyFranc,
    "currency_lira" to Icons.Filled.CurrencyLira,
    "currency_pound" to Icons.Filled.CurrencyPound,
    "currency_ruble" to Icons.Filled.CurrencyRuble,
    "currency_rupee" to Icons.Filled.CurrencyRupee,
    "currency_yen" to Icons.Filled.CurrencyYen,
    "currency_yuan" to Icons.Filled.CurrencyYuan,
    "curtains" to Icons.Filled.Curtains,
    "curtains_closed" to Icons.Filled.CurtainsClosed,
    "cyclone" to Icons.Filled.Cyclone,
    "dangerous" to Icons.Filled.Dangerous,
    "dark_mode" to Icons.Filled.DarkMode,
    "dashboard" to Icons.Filled.Dashboard,
    "dashboard_customize" to Icons.Filled.DashboardCustomize,
    "data_array" to Icons.Filled.DataArray,
    "data_exploration" to Icons.Filled.DataExploration,
    "data_object" to Icons.Filled.DataObject,
    "data_saver_off" to Icons.Filled.DataSaverOff,
    "data_saver_on" to Icons.Filled.DataSaverOn,
    "dataset" to Icons.Filled.Dataset,
    "dataset_linked" to Icons.Filled.DatasetLinked,
    "data_thresholding" to Icons.Filled.DataThresholding,
    "data_usage" to Icons.Filled.DataUsage,
    "deblur" to Icons.Filled.Deblur,
    "deck" to Icons.Filled.Deck,
    "dehaze" to Icons.Filled.Dehaze,
    "delete_forever" to Icons.Filled.DeleteForever,
    "delete_outline" to Icons.Filled.DeleteOutline,
    "delete_sweep" to Icons.Filled.DeleteSweep,
    "delivery_dining" to Icons.Filled.DeliveryDining,
    "density_large" to Icons.Filled.DensityLarge,
    "density_medium" to Icons.Filled.DensityMedium,
    "density_small" to Icons.Filled.DensitySmall,
    "departure_board" to Icons.Filled.DepartureBoard,
    "description" to Icons.Filled.Description,
    "deselect" to Icons.Filled.Deselect,
    "design_services" to Icons.Filled.DesignServices,
    "desk" to Icons.Filled.Desk,
    "desktop_access_disabled" to Icons.Filled.DesktopAccessDisabled,
    "desktop_mac" to Icons.Filled.DesktopMac,
    "desktop_windows" to Icons.Filled.DesktopWindows,
    "details" to Icons.Filled.Details,
    "developer_board" to Icons.Filled.DeveloperBoard,
    "developer_board_off" to Icons.Filled.DeveloperBoardOff,
    "developer_mode" to Icons.Filled.DeveloperMode,
    "device_hub" to Icons.Filled.DeviceHub,
    "devices" to Icons.Filled.Devices,
    "devices_fold" to Icons.Filled.DevicesFold,
    "devices_other" to Icons.Filled.DevicesOther,
    "device_thermostat" to Icons.Filled.DeviceThermostat,
    "device_unknown" to Icons.Filled.DeviceUnknown,
    "dialer_sip" to Icons.Filled.DialerSip,
    "dialpad" to Icons.Filled.Dialpad,
    "diamond" to Icons.Filled.Diamond,
    "difference" to Icons.Filled.Difference,
    "dining" to Icons.Filled.Dining,
    "dinner_dining" to Icons.Filled.DinnerDining,
    "directions" to Icons.Filled.Directions,
    "directions_bike" to Icons.Filled.DirectionsBike,
    "directions_boat" to Icons.Filled.DirectionsBoat,
    "directions_boat_filled" to Icons.Filled.DirectionsBoatFilled,
    "directions_bus" to Icons.Filled.DirectionsBus,
    "directions_bus_filled" to Icons.Filled.DirectionsBusFilled,
    "directions_car_filled" to Icons.Filled.DirectionsCarFilled,
    "directions_off" to Icons.Filled.DirectionsOff,
    "directions_railway" to Icons.Filled.DirectionsRailway,
    "directions_railway_filled" to Icons.Filled.DirectionsRailwayFilled,
    "directions_run" to Icons.Filled.DirectionsRun,
    "directions_subway" to Icons.Filled.DirectionsSubway,
    "directions_subway_filled" to Icons.Filled.DirectionsSubwayFilled,
    "directions_transit" to Icons.Filled.DirectionsTransit,
    "directions_transit_filled" to Icons.Filled.DirectionsTransitFilled,
    "directions_walk" to Icons.Filled.DirectionsWalk,
    "dirty_lens" to Icons.Filled.DirtyLens,
    "disabled_by_default" to Icons.Filled.DisabledByDefault,
    "disabled_visible" to Icons.Filled.DisabledVisible,
    "disc_full" to Icons.Filled.DiscFull,
    "discount" to Icons.Filled.Discount,
    "display_settings" to Icons.Filled.DisplaySettings,
    "diversity_1" to Icons.Filled.Diversity1,
    "diversity_2" to Icons.Filled.Diversity2,
    "diversity_3" to Icons.Filled.Diversity3,
    "dns" to Icons.Filled.Dns,
    "dock" to Icons.Filled.Dock,
    "document_scanner" to Icons.Filled.DocumentScanner,
    "do_disturb" to Icons.Filled.DoDisturb,
    "do_disturb_alt" to Icons.Filled.DoDisturbAlt,
    "do_disturb_off" to Icons.Filled.DoDisturbOff,
    "do_disturb_on" to Icons.Filled.DoDisturbOn,
    "domain" to Icons.Filled.Domain,
    "domain_add" to Icons.Filled.DomainAdd,
    "domain_disabled" to Icons.Filled.DomainDisabled,
    "domain_verification" to Icons.Filled.DomainVerification,
    "done_all" to Icons.Filled.DoneAll,
    "done_outline" to Icons.Filled.DoneOutline,
    "do_not_disturb" to Icons.Filled.DoNotDisturb,
    "do_not_disturb_alt" to Icons.Filled.DoNotDisturbAlt,
    "do_not_disturb_off" to Icons.Filled.DoNotDisturbOff,
    "do_not_disturb_on" to Icons.Filled.DoNotDisturbOn,
    "do_not_disturb_on_total_silence" to Icons.Filled.DoNotDisturbOnTotalSilence,
    "do_not_step" to Icons.Filled.DoNotStep,
    "do_not_touch" to Icons.Filled.DoNotTouch,
    "donut_large" to Icons.Filled.DonutLarge,
    "donut_small" to Icons.Filled.DonutSmall,
    "door_back" to Icons.Filled.DoorBack,
    "doorbell" to Icons.Filled.Doorbell,
    "door_front" to Icons.Filled.DoorFront,
    "door_sliding" to Icons.Filled.DoorSliding,
    "double_arrow" to Icons.Filled.DoubleArrow,
    "downhill_skiing" to Icons.Filled.DownhillSkiing,
    "download" to Icons.Filled.Download,
    "download_done" to Icons.Filled.DownloadDone,
    "download_for_offline" to Icons.Filled.DownloadForOffline,
    "downloading" to Icons.Filled.Downloading,
    "drafts" to Icons.Filled.Drafts,
    "drag_handle" to Icons.Filled.DragHandle,
    "drag_indicator" to Icons.Filled.DragIndicator,
    "draw" to Icons.Filled.Draw,
    "drive_eta" to Icons.Filled.DriveEta,
    "drive_file_move" to Icons.Filled.DriveFileMove,
    "drive_file_move_rtl" to Icons.Filled.DriveFileMoveRtl,
    "drive_file_rename_outline" to Icons.Filled.DriveFileRenameOutline,
    "drive_folder_upload" to Icons.Filled.DriveFolderUpload,
    "dry" to Icons.Filled.Dry,
    "dry_cleaning" to Icons.Filled.DryCleaning,
    "duo" to Icons.Filled.Duo,
    "dvr" to Icons.Filled.Dvr,
    "dynamic_feed" to Icons.Filled.DynamicFeed,
    "dynamic_form" to Icons.Filled.DynamicForm,
    "earbuds" to Icons.Filled.Earbuds,
    "earbuds_battery" to Icons.Filled.EarbudsBattery,
    "east" to Icons.Filled.East,
    "eco" to Icons.Filled.Eco,
    "edgesensor_high" to Icons.Filled.EdgesensorHigh,
    "edgesensor_low" to Icons.Filled.EdgesensorLow,
    "edit_attributes" to Icons.Filled.EditAttributes,
    "edit_calendar" to Icons.Filled.EditCalendar,
    "edit_location" to Icons.Filled.EditLocation,
    "edit_location_alt" to Icons.Filled.EditLocationAlt,
    "edit_note" to Icons.Filled.EditNote,
    "edit_notifications" to Icons.Filled.EditNotifications,
    "edit_off" to Icons.Filled.EditOff,
    "edit_road" to Icons.Filled.EditRoad,
    "egg" to Icons.Filled.Egg,
    "egg_alt" to Icons.Filled.EggAlt,
    "eject" to Icons.Filled.Eject,
    "elderly" to Icons.Filled.Elderly,
    "elderly_woman" to Icons.Filled.ElderlyWoman,
    "electrical_services" to Icons.Filled.ElectricalServices,
    "electric_bike" to Icons.Filled.ElectricBike,
    "electric_bolt" to Icons.Filled.ElectricBolt,
    "electric_car" to Icons.Filled.ElectricCar,
    "electric_meter" to Icons.Filled.ElectricMeter,
    "electric_moped" to Icons.Filled.ElectricMoped,
    "electric_rickshaw" to Icons.Filled.ElectricRickshaw,
    "electric_scooter" to Icons.Filled.ElectricScooter,
    "elevator" to Icons.Filled.Elevator,
    "emergency" to Icons.Filled.Emergency,
    "emergency_recording" to Icons.Filled.EmergencyRecording,
    "emergency_share" to Icons.Filled.EmergencyShare,
    "e_mobiledata" to Icons.Filled.EMobiledata,
    "emoji_emotions" to Icons.Filled.EmojiEmotions,
    "emoji_events" to Icons.Filled.EmojiEvents,
    "emoji_flags" to Icons.Filled.EmojiFlags,
    "emoji_food_beverage" to Icons.Filled.EmojiFoodBeverage,
    "emoji_nature" to Icons.Filled.EmojiNature,
    "emoji_objects" to Icons.Filled.EmojiObjects,
    "emoji_people" to Icons.Filled.EmojiPeople,
    "emoji_symbols" to Icons.Filled.EmojiSymbols,
    "emoji_transportation" to Icons.Filled.EmojiTransportation,
    "energy_savings_leaf" to Icons.Filled.EnergySavingsLeaf,
    "engineering" to Icons.Filled.Engineering,
    "enhanced_encryption" to Icons.Filled.EnhancedEncryption,
    "equalizer" to Icons.Filled.Equalizer,
    "error" to Icons.Filled.Error,
    "error_outline" to Icons.Filled.ErrorOutline,
    "escalator" to Icons.Filled.Escalator,
    "escalator_warning" to Icons.Filled.EscalatorWarning,
    "euro" to Icons.Filled.Euro,
    "euro_symbol" to Icons.Filled.EuroSymbol,
    "event" to Icons.Filled.Event,
    "event_available" to Icons.Filled.EventAvailable,
    "event_busy" to Icons.Filled.EventBusy,
    "event_note" to Icons.Filled.EventNote,
    "event_repeat" to Icons.Filled.EventRepeat,
    "event_seat" to Icons.Filled.EventSeat,
    "ev_station" to Icons.Filled.EvStation,
    "expand" to Icons.Filled.Expand,
    "expand_circle_down" to Icons.Filled.ExpandCircleDown,
    "expand_less" to Icons.Filled.ExpandLess,
    "expand_more" to Icons.Filled.ExpandMore,
    "explicit" to Icons.Filled.Explicit,
    "explore" to Icons.Filled.Explore,
    "explore_off" to Icons.Filled.ExploreOff,
    "exposure" to Icons.Filled.Exposure,
    "exposure_neg_1" to Icons.Filled.ExposureNeg1,
    "exposure_neg_2" to Icons.Filled.ExposureNeg2,
    "exposure_plus_1" to Icons.Filled.ExposurePlus1,
    "exposure_plus_2" to Icons.Filled.ExposurePlus2,
    "exposure_zero" to Icons.Filled.ExposureZero,
    "extension" to Icons.Filled.Extension,
    "extension_off" to Icons.Filled.ExtensionOff,
    "face_2" to Icons.Filled.Face2,
    "face_3" to Icons.Filled.Face3,
    "face_4" to Icons.Filled.Face4,
    "face_5" to Icons.Filled.Face5,
    "face_6" to Icons.Filled.Face6,
    "facebook" to Icons.Filled.Facebook,
    "face_retouching_natural" to Icons.Filled.FaceRetouchingNatural,
    "face_retouching_off" to Icons.Filled.FaceRetouchingOff,
    "fact_check" to Icons.Filled.FactCheck,
    "factory" to Icons.Filled.Factory,
    "family_restroom" to Icons.Filled.FamilyRestroom,
    "fastfood" to Icons.Filled.Fastfood,
    "fast_forward" to Icons.Filled.FastForward,
    "fast_rewind" to Icons.Filled.FastRewind,
    "fax" to Icons.Filled.Fax,
    "featured_play_list" to Icons.Filled.FeaturedPlayList,
    "featured_video" to Icons.Filled.FeaturedVideo,
    "feed" to Icons.Filled.Feed,
    "feedback" to Icons.Filled.Feedback,
    "female" to Icons.Filled.Female,
    "fence" to Icons.Filled.Fence,
    "festival" to Icons.Filled.Festival,
    "fiber_dvr" to Icons.Filled.FiberDvr,
    "fiber_manual_record" to Icons.Filled.FiberManualRecord,
    "fiber_new" to Icons.Filled.FiberNew,
    "fiber_pin" to Icons.Filled.FiberPin,
    "fiber_smart_record" to Icons.Filled.FiberSmartRecord,
    "file_copy" to Icons.Filled.FileCopy,
    "file_download" to Icons.Filled.FileDownload,
    "file_download_done" to Icons.Filled.FileDownloadDone,
    "file_download_off" to Icons.Filled.FileDownloadOff,
    "file_open" to Icons.Filled.FileOpen,
    "file_present" to Icons.Filled.FilePresent,
    "file_upload" to Icons.Filled.FileUpload,
    "filter" to Icons.Filled.Filter,
    "filter_1" to Icons.Filled.Filter1,
    "filter_2" to Icons.Filled.Filter2,
    "filter_3" to Icons.Filled.Filter3,
    "filter_4" to Icons.Filled.Filter4,
    "filter_5" to Icons.Filled.Filter5,
    "filter_6" to Icons.Filled.Filter6,
    "filter_7" to Icons.Filled.Filter7,
    "filter_8" to Icons.Filled.Filter8,
    "filter_9" to Icons.Filled.Filter9,
    "filter_9_plus" to Icons.Filled.Filter9Plus,
    "filter_alt" to Icons.Filled.FilterAlt,
    "filter_alt_off" to Icons.Filled.FilterAltOff,
    "filter_b_and_w" to Icons.Filled.FilterBAndW,
    "filter_center_focus" to Icons.Filled.FilterCenterFocus,
    "filter_drama" to Icons.Filled.FilterDrama,
    "filter_frames" to Icons.Filled.FilterFrames,
    "filter_hdr" to Icons.Filled.FilterHdr,
    "filter_list" to Icons.Filled.FilterList,
    "filter_list_off" to Icons.Filled.FilterListOff,
    "filter_none" to Icons.Filled.FilterNone,
    "filter_tilt_shift" to Icons.Filled.FilterTiltShift,
    "filter_vintage" to Icons.Filled.FilterVintage,
    "find_in_page" to Icons.Filled.FindInPage,
    "find_replace" to Icons.Filled.FindReplace,
    "fingerprint" to Icons.Filled.Fingerprint,
    "fire_extinguisher" to Icons.Filled.FireExtinguisher,
    "fire_hydrant_alt" to Icons.Filled.FireHydrantAlt,
    "fireplace" to Icons.Filled.Fireplace,
    "fire_truck" to Icons.Filled.FireTruck,
    "first_page" to Icons.Filled.FirstPage,
    "fitbit" to Icons.Filled.Fitbit,
    "fitness_center" to Icons.Filled.FitnessCenter,
    "fit_screen" to Icons.Filled.FitScreen,
    "flag" to Icons.Filled.Flag,
    "flag_circle" to Icons.Filled.FlagCircle,
    "flaky" to Icons.Filled.Flaky,
    "flare" to Icons.Filled.Flare,
    "flash_auto" to Icons.Filled.FlashAuto,
    "flashlight_off" to Icons.Filled.FlashlightOff,
    "flashlight_on" to Icons.Filled.FlashlightOn,
    "flash_off" to Icons.Filled.FlashOff,
    "flash_on" to Icons.Filled.FlashOn,
    "flatware" to Icons.Filled.Flatware,
    "flight_class" to Icons.Filled.FlightClass,
    "flight_land" to Icons.Filled.FlightLand,
    "flight_takeoff" to Icons.Filled.FlightTakeoff,
    "flip" to Icons.Filled.Flip,
    "flip_camera_android" to Icons.Filled.FlipCameraAndroid,
    "flip_camera_ios" to Icons.Filled.FlipCameraIos,
    "flip_to_back" to Icons.Filled.FlipToBack,
    "flip_to_front" to Icons.Filled.FlipToFront,
    "flood" to Icons.Filled.Flood,
    "flourescent" to Icons.Filled.Flourescent,
    "fluorescent" to Icons.Filled.Fluorescent,
    "flutter_dash" to Icons.Filled.FlutterDash,
    "fmd_bad" to Icons.Filled.FmdBad,
    "fmd_good" to Icons.Filled.FmdGood,
    "folder" to Icons.Filled.Folder,
    "folder_copy" to Icons.Filled.FolderCopy,
    "folder_delete" to Icons.Filled.FolderDelete,
    "folder_off" to Icons.Filled.FolderOff,
    "folder_open" to Icons.Filled.FolderOpen,
    "folder_shared" to Icons.Filled.FolderShared,
    "folder_special" to Icons.Filled.FolderSpecial,
    "folder_zip" to Icons.Filled.FolderZip,
    "follow_the_signs" to Icons.Filled.FollowTheSigns,
    "font_download" to Icons.Filled.FontDownload,
    "font_download_off" to Icons.Filled.FontDownloadOff,
    "food_bank" to Icons.Filled.FoodBank,
    "fork_left" to Icons.Filled.ForkLeft,
    "fork_right" to Icons.Filled.ForkRight,
    "format_align_center" to Icons.Filled.FormatAlignCenter,
    "format_align_justify" to Icons.Filled.FormatAlignJustify,
    "format_align_left" to Icons.Filled.FormatAlignLeft,
    "format_align_right" to Icons.Filled.FormatAlignRight,
    "format_bold" to Icons.Filled.FormatBold,
    "format_clear" to Icons.Filled.FormatClear,
    "format_color_fill" to Icons.Filled.FormatColorFill,
    "format_color_reset" to Icons.Filled.FormatColorReset,
    "format_color_text" to Icons.Filled.FormatColorText,
    "format_indent_decrease" to Icons.Filled.FormatIndentDecrease,
    "format_indent_increase" to Icons.Filled.FormatIndentIncrease,
    "format_italic" to Icons.Filled.FormatItalic,
    "format_line_spacing" to Icons.Filled.FormatLineSpacing,
    "format_list_bulleted" to Icons.Filled.FormatListBulleted,
    "format_list_numbered" to Icons.Filled.FormatListNumbered,
    "format_list_numbered_rtl" to Icons.Filled.FormatListNumberedRtl,
    "format_overline" to Icons.Filled.FormatOverline,
    "format_paint" to Icons.Filled.FormatPaint,
    "format_quote" to Icons.Filled.FormatQuote,
    "format_shapes" to Icons.Filled.FormatShapes,
    "format_size" to Icons.Filled.FormatSize,
    "format_strikethrough" to Icons.Filled.FormatStrikethrough,
    "format_textdirection_l_to_r" to Icons.Filled.FormatTextdirectionLToR,
    "format_textdirection_r_to_l" to Icons.Filled.FormatTextdirectionRToL,
    "format_underlined" to Icons.Filled.FormatUnderlined,
    "fort" to Icons.Filled.Fort,
    "forum" to Icons.Filled.Forum,
    "forward" to Icons.Filled.Forward,
    "forward_10" to Icons.Filled.Forward10,
    "forward_30" to Icons.Filled.Forward30,
    "forward_5" to Icons.Filled.Forward5,
    "forward_to_inbox" to Icons.Filled.ForwardToInbox,
    "foundation" to Icons.Filled.Foundation,
    "free_breakfast" to Icons.Filled.FreeBreakfast,
    "free_cancellation" to Icons.Filled.FreeCancellation,
    "front_hand" to Icons.Filled.FrontHand,
    "fullscreen" to Icons.Filled.Fullscreen,
    "fullscreen_exit" to Icons.Filled.FullscreenExit,
    "functions" to Icons.Filled.Functions,
    "gamepad" to Icons.Filled.Gamepad,
    "garage" to Icons.Filled.Garage,
    "gas_meter" to Icons.Filled.GasMeter,
    "gavel" to Icons.Filled.Gavel,
    "generating_tokens" to Icons.Filled.GeneratingTokens,
    "gesture" to Icons.Filled.Gesture,
    "get_app" to Icons.Filled.GetApp,
    "gif" to Icons.Filled.Gif,
    "gif_box" to Icons.Filled.GifBox,
    "girl" to Icons.Filled.Girl,
    "gite" to Icons.Filled.Gite,
    "g_mobiledata" to Icons.Filled.GMobiledata,
    "golf_course" to Icons.Filled.GolfCourse,
    "gpp_bad" to Icons.Filled.GppBad,
    "gpp_good" to Icons.Filled.GppGood,
    "gpp_maybe" to Icons.Filled.GppMaybe,
    "gps_fixed" to Icons.Filled.GpsFixed,
    "gps_not_fixed" to Icons.Filled.GpsNotFixed,
    "gps_off" to Icons.Filled.GpsOff,
    "grade" to Icons.Filled.Grade,
    "gradient" to Icons.Filled.Gradient,
    "grading" to Icons.Filled.Grading,
    "grain" to Icons.Filled.Grain,
    "graphic_eq" to Icons.Filled.GraphicEq,
    "grass" to Icons.Filled.Grass,
    "grid_3_x_3" to Icons.Filled.Grid3x3,
    "grid_4_x_4" to Icons.Filled.Grid4x4,
    "grid_goldenratio" to Icons.Filled.GridGoldenratio,
    "grid_off" to Icons.Filled.GridOff,
    "grid_on" to Icons.Filled.GridOn,
    "grid_view" to Icons.Filled.GridView,
    "group_add" to Icons.Filled.GroupAdd,
    "group_off" to Icons.Filled.GroupOff,
    "group_remove" to Icons.Filled.GroupRemove,
    "groups" to Icons.Filled.Groups,
    "groups_2" to Icons.Filled.Groups2,
    "groups_3" to Icons.Filled.Groups3,
    "group_work" to Icons.Filled.GroupWork,
    "g_translate" to Icons.Filled.GTranslate,
    "hail" to Icons.Filled.Hail,
    "handshake" to Icons.Filled.Handshake,
    "handyman" to Icons.Filled.Handyman,
    "hardware" to Icons.Filled.Hardware,
    "hd" to Icons.Filled.Hd,
    "hdr_auto" to Icons.Filled.HdrAuto,
    "hdr_auto_select" to Icons.Filled.HdrAutoSelect,
    "hdr_enhanced_select" to Icons.Filled.HdrEnhancedSelect,
    "hdr_off" to Icons.Filled.HdrOff,
    "hdr_off_select" to Icons.Filled.HdrOffSelect,
    "hdr_on" to Icons.Filled.HdrOn,
    "hdr_on_select" to Icons.Filled.HdrOnSelect,
    "hdr_plus" to Icons.Filled.HdrPlus,
    "hdr_strong" to Icons.Filled.HdrStrong,
    "hdr_weak" to Icons.Filled.HdrWeak,
    "headphones_battery" to Icons.Filled.HeadphonesBattery,
    "headset" to Icons.Filled.Headset,
    "headset_mic" to Icons.Filled.HeadsetMic,
    "headset_off" to Icons.Filled.HeadsetOff,
    "healing" to Icons.Filled.Healing,
    "health_and_safety" to Icons.Filled.HealthAndSafety,
    "hearing" to Icons.Filled.Hearing,
    "hearing_disabled" to Icons.Filled.HearingDisabled,
    "heart_broken" to Icons.Filled.HeartBroken,
    "heat_pump" to Icons.Filled.HeatPump,
    "height" to Icons.Filled.Height,
    "help" to Icons.Filled.Help,
    "help_center" to Icons.Filled.HelpCenter,
    "help_outline" to Icons.Filled.HelpOutline,
    "hevc" to Icons.Filled.Hevc,
    "hexagon" to Icons.Filled.Hexagon,
    "hide_image" to Icons.Filled.HideImage,
    "hide_source" to Icons.Filled.HideSource,
    "highlight" to Icons.Filled.Highlight,
    "highlight_alt" to Icons.Filled.HighlightAlt,
    "highlight_off" to Icons.Filled.HighlightOff,
    "high_quality" to Icons.Filled.HighQuality,
    "hiking" to Icons.Filled.Hiking,
    "history" to Icons.Filled.History,
    "history_edu" to Icons.Filled.HistoryEdu,
    "history_toggle_off" to Icons.Filled.HistoryToggleOff,
    "hive" to Icons.Filled.Hive,
    "hls" to Icons.Filled.Hls,
    "hls_off" to Icons.Filled.HlsOff,
    "h_mobiledata" to Icons.Filled.HMobiledata,
    "holiday_village" to Icons.Filled.HolidayVillage,
    "home_max" to Icons.Filled.HomeMax,
    "home_mini" to Icons.Filled.HomeMini,
    "home_repair_service" to Icons.Filled.HomeRepairService,
    "home_work" to Icons.Filled.HomeWork,
    "horizontal_distribute" to Icons.Filled.HorizontalDistribute,
    "horizontal_rule" to Icons.Filled.HorizontalRule,
    "horizontal_split" to Icons.Filled.HorizontalSplit,
    "hotel" to Icons.Filled.Hotel,
    "hotel_class" to Icons.Filled.HotelClass,
    "hot_tub" to Icons.Filled.HotTub,
    "hourglass_bottom" to Icons.Filled.HourglassBottom,
    "hourglass_disabled" to Icons.Filled.HourglassDisabled,
    "hourglass_empty" to Icons.Filled.HourglassEmpty,
    "hourglass_full" to Icons.Filled.HourglassFull,
    "hourglass_top" to Icons.Filled.HourglassTop,
    "house" to Icons.Filled.House,
    "houseboat" to Icons.Filled.Houseboat,
    "house_siding" to Icons.Filled.HouseSiding,
    "how_to_reg" to Icons.Filled.HowToReg,
    "how_to_vote" to Icons.Filled.HowToVote,
    "h_plus_mobiledata" to Icons.Filled.HPlusMobiledata,
    "html" to Icons.Filled.Html,
    "http" to Icons.Filled.Http,
    "https" to Icons.Filled.Https,
    "hub" to Icons.Filled.Hub,
    "hvac" to Icons.Filled.Hvac,
    "icecream" to Icons.Filled.Icecream,
    "ice_skating" to Icons.Filled.IceSkating,
    "image" to Icons.Filled.Image,
    "image_aspect_ratio" to Icons.Filled.ImageAspectRatio,
    "image_not_supported" to Icons.Filled.ImageNotSupported,
    "image_search" to Icons.Filled.ImageSearch,
    "imagesearch_roller" to Icons.Filled.ImagesearchRoller,
    "important_devices" to Icons.Filled.ImportantDevices,
    "import_contacts" to Icons.Filled.ImportContacts,
    "import_export" to Icons.Filled.ImportExport,
    "inbox" to Icons.Filled.Inbox,
    "incomplete_circle" to Icons.Filled.IncompleteCircle,
    "indeterminate_check_box" to Icons.Filled.IndeterminateCheckBox,
    "input" to Icons.Filled.Input,
    "insert_chart" to Icons.Filled.InsertChart,
    "insert_chart_outlined" to Icons.Filled.InsertChartOutlined,
    "insert_comment" to Icons.Filled.InsertComment,
    "insert_drive_file" to Icons.Filled.InsertDriveFile,
    "insert_emoticon" to Icons.Filled.InsertEmoticon,
    "insert_invitation" to Icons.Filled.InsertInvitation,
    "insert_link" to Icons.Filled.InsertLink,
    "insert_page_break" to Icons.Filled.InsertPageBreak,
    "insert_photo" to Icons.Filled.InsertPhoto,
    "insights" to Icons.Filled.Insights,
    "install_desktop" to Icons.Filled.InstallDesktop,
    "install_mobile" to Icons.Filled.InstallMobile,
    "integration_instructions" to Icons.Filled.IntegrationInstructions,
    "interests" to Icons.Filled.Interests,
    "interpreter_mode" to Icons.Filled.InterpreterMode,
    "inventory" to Icons.Filled.Inventory,
    "inventory_2" to Icons.Filled.Inventory2,
    "invert_colors" to Icons.Filled.InvertColors,
    "invert_colors_off" to Icons.Filled.InvertColorsOff,
    "ios_share" to Icons.Filled.IosShare,
    "iron" to Icons.Filled.Iron,
    "iso" to Icons.Filled.Iso,
    "javascript" to Icons.Filled.Javascript,
    "join_full" to Icons.Filled.JoinFull,
    "join_inner" to Icons.Filled.JoinInner,
    "join_left" to Icons.Filled.JoinLeft,
    "join_right" to Icons.Filled.JoinRight,
    "kayaking" to Icons.Filled.Kayaking,
    "kebab_dining" to Icons.Filled.KebabDining,
    "key" to Icons.Filled.Key,
    "keyboard" to Icons.Filled.Keyboard,
    "keyboard_alt" to Icons.Filled.KeyboardAlt,
    "keyboard_backspace" to Icons.Filled.KeyboardBackspace,
    "keyboard_capslock" to Icons.Filled.KeyboardCapslock,
    "keyboard_command_key" to Icons.Filled.KeyboardCommandKey,
    "keyboard_control_key" to Icons.Filled.KeyboardControlKey,
    "keyboard_double_arrow_down" to Icons.Filled.KeyboardDoubleArrowDown,
    "keyboard_double_arrow_left" to Icons.Filled.KeyboardDoubleArrowLeft,
    "keyboard_double_arrow_right" to Icons.Filled.KeyboardDoubleArrowRight,
    "keyboard_double_arrow_up" to Icons.Filled.KeyboardDoubleArrowUp,
    "keyboard_hide" to Icons.Filled.KeyboardHide,
    "keyboard_option_key" to Icons.Filled.KeyboardOptionKey,
    "keyboard_return" to Icons.Filled.KeyboardReturn,
    "keyboard_tab" to Icons.Filled.KeyboardTab,
    "keyboard_voice" to Icons.Filled.KeyboardVoice,
    "key_off" to Icons.Filled.KeyOff,
    "king_bed" to Icons.Filled.KingBed,
    "kitchen" to Icons.Filled.Kitchen,
    "kitesurfing" to Icons.Filled.Kitesurfing,
    "label" to Icons.Filled.Label,
    "label_important" to Icons.Filled.LabelImportant,
    "label_off" to Icons.Filled.LabelOff,
    "lan" to Icons.Filled.Lan,
    "landscape" to Icons.Filled.Landscape,
    "landslide" to Icons.Filled.Landslide,
    "laptop" to Icons.Filled.Laptop,
    "laptop_chromebook" to Icons.Filled.LaptopChromebook,
    "laptop_mac" to Icons.Filled.LaptopMac,
    "laptop_windows" to Icons.Filled.LaptopWindows,
    "last_page" to Icons.Filled.LastPage,
    "launch" to Icons.Filled.Launch,
    "layers" to Icons.Filled.Layers,
    "layers_clear" to Icons.Filled.LayersClear,
    "leaderboard" to Icons.Filled.Leaderboard,
    "leak_add" to Icons.Filled.LeakAdd,
    "leak_remove" to Icons.Filled.LeakRemove,
    "leave_bags_at_home" to Icons.Filled.LeaveBagsAtHome,
    "legend_toggle" to Icons.Filled.LegendToggle,
    "lens" to Icons.Filled.Lens,
    "lens_blur" to Icons.Filled.LensBlur,
    "library_add" to Icons.Filled.LibraryAdd,
    "library_add_check" to Icons.Filled.LibraryAddCheck,
    "library_books" to Icons.Filled.LibraryBooks,
    "library_music" to Icons.Filled.LibraryMusic,
    "light" to Icons.Filled.Light,
    "lightbulb_circle" to Icons.Filled.LightbulbCircle,
    "light_mode" to Icons.Filled.LightMode,
    "linear_scale" to Icons.Filled.LinearScale,
    "line_axis" to Icons.Filled.LineAxis,
    "line_style" to Icons.Filled.LineStyle,
    "line_weight" to Icons.Filled.LineWeight,
    "link" to Icons.Filled.Link,
    "linked_camera" to Icons.Filled.LinkedCamera,
    "link_off" to Icons.Filled.LinkOff,
    "liquor" to Icons.Filled.Liquor,
    "list_alt" to Icons.Filled.ListAlt,
    "live_help" to Icons.Filled.LiveHelp,
    "live_tv" to Icons.Filled.LiveTv,
    "living" to Icons.Filled.Living,
    "local_activity" to Icons.Filled.LocalActivity,
    "local_airport" to Icons.Filled.LocalAirport,
    "local_atm" to Icons.Filled.LocalAtm,
    "local_bar" to Icons.Filled.LocalBar,
    "local_car_wash" to Icons.Filled.LocalCarWash,
    "local_convenience_store" to Icons.Filled.LocalConvenienceStore,
    "local_dining" to Icons.Filled.LocalDining,
    "local_drink" to Icons.Filled.LocalDrink,
    "local_fire_department" to Icons.Filled.LocalFireDepartment,
    "local_florist" to Icons.Filled.LocalFlorist,
    "local_gas_station" to Icons.Filled.LocalGasStation,
    "local_grocery_store" to Icons.Filled.LocalGroceryStore,
    "local_hospital" to Icons.Filled.LocalHospital,
    "local_hotel" to Icons.Filled.LocalHotel,
    "local_laundry_service" to Icons.Filled.LocalLaundryService,
    "local_library" to Icons.Filled.LocalLibrary,
    "local_mall" to Icons.Filled.LocalMall,
    "local_movies" to Icons.Filled.LocalMovies,
    "local_offer" to Icons.Filled.LocalOffer,
    "local_parking" to Icons.Filled.LocalParking,
    "local_pharmacy" to Icons.Filled.LocalPharmacy,
    "local_phone" to Icons.Filled.LocalPhone,
    "local_pizza" to Icons.Filled.LocalPizza,
    "local_play" to Icons.Filled.LocalPlay,
    "local_police" to Icons.Filled.LocalPolice,
    "local_post_office" to Icons.Filled.LocalPostOffice,
    "local_printshop" to Icons.Filled.LocalPrintshop,
    "local_see" to Icons.Filled.LocalSee,
    "local_shipping" to Icons.Filled.LocalShipping,
    "local_taxi" to Icons.Filled.LocalTaxi,
    "location_city" to Icons.Filled.LocationCity,
    "location_disabled" to Icons.Filled.LocationDisabled,
    "location_off" to Icons.Filled.LocationOff,
    "location_searching" to Icons.Filled.LocationSearching,
    "lock_clock" to Icons.Filled.LockClock,
    "lock_open" to Icons.Filled.LockOpen,
    "lock_person" to Icons.Filled.LockPerson,
    "lock_reset" to Icons.Filled.LockReset,
    "login" to Icons.Filled.Login,
    "logo_dev" to Icons.Filled.LogoDev,
    "logout" to Icons.Filled.Logout,
    "looks" to Icons.Filled.Looks,
    "looks_3" to Icons.Filled.Looks3,
    "looks_4" to Icons.Filled.Looks4,
    "looks_5" to Icons.Filled.Looks5,
    "looks_6" to Icons.Filled.Looks6,
    "looks_one" to Icons.Filled.LooksOne,
    "looks_two" to Icons.Filled.LooksTwo,
    "loop" to Icons.Filled.Loop,
    "loupe" to Icons.Filled.Loupe,
    "low_priority" to Icons.Filled.LowPriority,
    "loyalty" to Icons.Filled.Loyalty,
    "lte_mobiledata" to Icons.Filled.LteMobiledata,
    "lte_plus_mobiledata" to Icons.Filled.LtePlusMobiledata,
    "luggage" to Icons.Filled.Luggage,
    "lunch_dining" to Icons.Filled.LunchDining,
    "lyrics" to Icons.Filled.Lyrics,
    "macro_off" to Icons.Filled.MacroOff,
    "mail_lock" to Icons.Filled.MailLock,
    "male" to Icons.Filled.Male,
    "man" to Icons.Filled.Man,
    "man_2" to Icons.Filled.Man2,
    "man_3" to Icons.Filled.Man3,
    "man_4" to Icons.Filled.Man4,
    "manage_accounts" to Icons.Filled.ManageAccounts,
    "manage_history" to Icons.Filled.ManageHistory,
    "manage_search" to Icons.Filled.ManageSearch,
    "maps_home_work" to Icons.Filled.MapsHomeWork,
    "maps_ugc" to Icons.Filled.MapsUgc,
    "margin" to Icons.Filled.Margin,
    "mark_as_unread" to Icons.Filled.MarkAsUnread,
    "mark_chat_read" to Icons.Filled.MarkChatRead,
    "mark_chat_unread" to Icons.Filled.MarkChatUnread,
    "mark_email_read" to Icons.Filled.MarkEmailRead,
    "mark_email_unread" to Icons.Filled.MarkEmailUnread,
    "markunread" to Icons.Filled.Markunread,
    "mark_unread_chat_alt" to Icons.Filled.MarkUnreadChatAlt,
    "markunread_mailbox" to Icons.Filled.MarkunreadMailbox,
    "masks" to Icons.Filled.Masks,
    "maximize" to Icons.Filled.Maximize,
    "media_bluetooth_off" to Icons.Filled.MediaBluetoothOff,
    "media_bluetooth_on" to Icons.Filled.MediaBluetoothOn,
    "mediation" to Icons.Filled.Mediation,
    "medical_information" to Icons.Filled.MedicalInformation,
    "medical_services" to Icons.Filled.MedicalServices,
    "medication" to Icons.Filled.Medication,
    "meeting_room" to Icons.Filled.MeetingRoom,
    "memory" to Icons.Filled.Memory,
    "menu_book" to Icons.Filled.MenuBook,
    "menu_open" to Icons.Filled.MenuOpen,
    "merge" to Icons.Filled.Merge,
    "merge_type" to Icons.Filled.MergeType,
    "message" to Icons.Filled.Message,
    "mic" to Icons.Filled.Mic,
    "mic_external_off" to Icons.Filled.MicExternalOff,
    "mic_external_on" to Icons.Filled.MicExternalOn,
    "mic_none" to Icons.Filled.MicNone,
    "mic_off" to Icons.Filled.MicOff,
    "microwave" to Icons.Filled.Microwave,
    "military_tech" to Icons.Filled.MilitaryTech,
    "minimize" to Icons.Filled.Minimize,
    "minor_crash" to Icons.Filled.MinorCrash,
    "miscellaneous_services" to Icons.Filled.MiscellaneousServices,
    "missed_video_call" to Icons.Filled.MissedVideoCall,
    "mms" to Icons.Filled.Mms,
    "mobiledata_off" to Icons.Filled.MobiledataOff,
    "mobile_friendly" to Icons.Filled.MobileFriendly,
    "mobile_off" to Icons.Filled.MobileOff,
    "mobile_screen_share" to Icons.Filled.MobileScreenShare,
    "mode" to Icons.Filled.Mode,
    "mode_comment" to Icons.Filled.ModeComment,
    "mode_edit" to Icons.Filled.ModeEdit,
    "mode_edit_outline" to Icons.Filled.ModeEditOutline,
    "mode_fan_off" to Icons.Filled.ModeFanOff,
    "model_training" to Icons.Filled.ModelTraining,
    "mode_night" to Icons.Filled.ModeNight,
    "mode_of_travel" to Icons.Filled.ModeOfTravel,
    "mode_standby" to Icons.Filled.ModeStandby,
    "monetization_on" to Icons.Filled.MonetizationOn,
    "money_off" to Icons.Filled.MoneyOff,
    "money_off_csred" to Icons.Filled.MoneyOffCsred,
    "monitor" to Icons.Filled.Monitor,
    "monitor_heart" to Icons.Filled.MonitorHeart,
    "monitor_weight" to Icons.Filled.MonitorWeight,
    "monochrome_photos" to Icons.Filled.MonochromePhotos,
    "mood" to Icons.Filled.Mood,
    "mood_bad" to Icons.Filled.MoodBad,
    "moped" to Icons.Filled.Moped,
    "more" to Icons.Filled.More,
    "more_horiz" to Icons.Filled.MoreHoriz,
    "more_time" to Icons.Filled.MoreTime,
    "mosque" to Icons.Filled.Mosque,
    "motion_photos_auto" to Icons.Filled.MotionPhotosAuto,
    "motion_photos_off" to Icons.Filled.MotionPhotosOff,
    "motion_photos_on" to Icons.Filled.MotionPhotosOn,
    "motion_photos_pause" to Icons.Filled.MotionPhotosPause,
    "motion_photos_paused" to Icons.Filled.MotionPhotosPaused,
    "motorcycle" to Icons.Filled.Motorcycle,
    "mouse" to Icons.Filled.Mouse,
    "move_down" to Icons.Filled.MoveDown,
    "move_to_inbox" to Icons.Filled.MoveToInbox,
    "move_up" to Icons.Filled.MoveUp,
    "movie_creation" to Icons.Filled.MovieCreation,
    "movie_filter" to Icons.Filled.MovieFilter,
    "moving" to Icons.Filled.Moving,
    "mp" to Icons.Filled.Mp,
    "multiline_chart" to Icons.Filled.MultilineChart,
    "multiple_stop" to Icons.Filled.MultipleStop,
    "museum" to Icons.Filled.Museum,
    "music_note" to Icons.Filled.MusicNote,
    "music_off" to Icons.Filled.MusicOff,
    "music_video" to Icons.Filled.MusicVideo,
    "my_location" to Icons.Filled.MyLocation,
    "nat" to Icons.Filled.Nat,
    "nature_people" to Icons.Filled.NaturePeople,
    "navigate_before" to Icons.Filled.NavigateBefore,
    "navigate_next" to Icons.Filled.NavigateNext,
    "navigation" to Icons.Filled.Navigation,
    "nearby_error" to Icons.Filled.NearbyError,
    "nearby_off" to Icons.Filled.NearbyOff,
    "near_me" to Icons.Filled.NearMe,
    "near_me_disabled" to Icons.Filled.NearMeDisabled,
    "nest_cam_wired_stand" to Icons.Filled.NestCamWiredStand,
    "network_cell" to Icons.Filled.NetworkCell,
    "network_check" to Icons.Filled.NetworkCheck,
    "network_locked" to Icons.Filled.NetworkLocked,
    "network_ping" to Icons.Filled.NetworkPing,
    "network_wifi" to Icons.Filled.NetworkWifi,
    "network_wifi_1_bar" to Icons.Filled.NetworkWifi1Bar,
    "network_wifi_2_bar" to Icons.Filled.NetworkWifi2Bar,
    "network_wifi_3_bar" to Icons.Filled.NetworkWifi3Bar,
    "new_label" to Icons.Filled.NewLabel,
    "new_releases" to Icons.Filled.NewReleases,
    "newspaper" to Icons.Filled.Newspaper,
    "next_plan" to Icons.Filled.NextPlan,
    "next_week" to Icons.Filled.NextWeek,
    "nfc" to Icons.Filled.Nfc,
    "nightlife" to Icons.Filled.Nightlife,
    "nightlight_round" to Icons.Filled.NightlightRound,
    "night_shelter" to Icons.Filled.NightShelter,
    "nights_stay" to Icons.Filled.NightsStay,
    "no_accounts" to Icons.Filled.NoAccounts,
    "no_adult_content" to Icons.Filled.NoAdultContent,
    "no_backpack" to Icons.Filled.NoBackpack,
    "no_cell" to Icons.Filled.NoCell,
    "no_crash" to Icons.Filled.NoCrash,
    "no_drinks" to Icons.Filled.NoDrinks,
    "no_encryption" to Icons.Filled.NoEncryption,
    "no_encryption_gmailerrorred" to Icons.Filled.NoEncryptionGmailerrorred,
    "no_flash" to Icons.Filled.NoFlash,
    "no_food" to Icons.Filled.NoFood,
    "noise_aware" to Icons.Filled.NoiseAware,
    "noise_control_off" to Icons.Filled.NoiseControlOff,
    "no_luggage" to Icons.Filled.NoLuggage,
    "no_meals" to Icons.Filled.NoMeals,
    "no_meeting_room" to Icons.Filled.NoMeetingRoom,
    "no_photography" to Icons.Filled.NoPhotography,
    "nordic_walking" to Icons.Filled.NordicWalking,
    "north" to Icons.Filled.North,
    "north_east" to Icons.Filled.NorthEast,
    "north_west" to Icons.Filled.NorthWest,
    "no_sim" to Icons.Filled.NoSim,
    "no_stroller" to Icons.Filled.NoStroller,
    "not_accessible" to Icons.Filled.NotAccessible,
    "note" to Icons.Filled.Note,
    "note_add" to Icons.Filled.NoteAdd,
    "note_alt" to Icons.Filled.NoteAlt,
    "notes" to Icons.Filled.Notes,
    "notification_add" to Icons.Filled.NotificationAdd,
    "notification_important" to Icons.Filled.NotificationImportant,
    "notifications_active" to Icons.Filled.NotificationsActive,
    "notifications_none" to Icons.Filled.NotificationsNone,
    "notifications_off" to Icons.Filled.NotificationsOff,
    "notifications_paused" to Icons.Filled.NotificationsPaused,
    "not_interested" to Icons.Filled.NotInterested,
    "not_listed_location" to Icons.Filled.NotListedLocation,
    "no_transfer" to Icons.Filled.NoTransfer,
    "not_started" to Icons.Filled.NotStarted,
    "numbers" to Icons.Filled.Numbers,
    "offline_bolt" to Icons.Filled.OfflineBolt,
    "offline_pin" to Icons.Filled.OfflinePin,
    "offline_share" to Icons.Filled.OfflineShare,
    "oil_barrel" to Icons.Filled.OilBarrel,
    "ondemand_video" to Icons.Filled.OndemandVideo,
    "on_device_training" to Icons.Filled.OnDeviceTraining,
    "online_prediction" to Icons.Filled.OnlinePrediction,
    "opacity" to Icons.Filled.Opacity,
    "open_in_browser" to Icons.Filled.OpenInBrowser,
    "open_in_full" to Icons.Filled.OpenInFull,
    "open_in_new" to Icons.Filled.OpenInNew,
    "open_in_new_off" to Icons.Filled.OpenInNewOff,
    "open_with" to Icons.Filled.OpenWith,
    "other_houses" to Icons.Filled.OtherHouses,
    "outbond" to Icons.Filled.Outbond,
    "outbound" to Icons.Filled.Outbound,
    "outbox" to Icons.Filled.Outbox,
    "outdoor_grill" to Icons.Filled.OutdoorGrill,
    "outlet" to Icons.Filled.Outlet,
    "outlined_flag" to Icons.Filled.OutlinedFlag,
    "output" to Icons.Filled.Output,
    "padding" to Icons.Filled.Padding,
    "pages" to Icons.Filled.Pages,
    "pageview" to Icons.Filled.Pageview,
    "paid" to Icons.Filled.Paid,
    "panorama" to Icons.Filled.Panorama,
    "panorama_fish_eye" to Icons.Filled.PanoramaFishEye,
    "panorama_horizontal" to Icons.Filled.PanoramaHorizontal,
    "panorama_horizontal_select" to Icons.Filled.PanoramaHorizontalSelect,
    "panorama_photosphere" to Icons.Filled.PanoramaPhotosphere,
    "panorama_photosphere_select" to Icons.Filled.PanoramaPhotosphereSelect,
    "panorama_vertical" to Icons.Filled.PanoramaVertical,
    "panorama_vertical_select" to Icons.Filled.PanoramaVerticalSelect,
    "panorama_wide_angle" to Icons.Filled.PanoramaWideAngle,
    "panorama_wide_angle_select" to Icons.Filled.PanoramaWideAngleSelect,
    "pan_tool" to Icons.Filled.PanTool,
    "pan_tool_alt" to Icons.Filled.PanToolAlt,
    "paragliding" to Icons.Filled.Paragliding,
    "park" to Icons.Filled.Park,
    "party_mode" to Icons.Filled.PartyMode,
    "password" to Icons.Filled.Password,
    "pattern" to Icons.Filled.Pattern,
    "pause" to Icons.Filled.Pause,
    "pause_circle" to Icons.Filled.PauseCircle,
    "pause_circle_filled" to Icons.Filled.PauseCircleFilled,
    "pause_circle_outline" to Icons.Filled.PauseCircleOutline,
    "pause_presentation" to Icons.Filled.PausePresentation,
    "payment" to Icons.Filled.Payment,
    "payments" to Icons.Filled.Payments,
    "pedal_bike" to Icons.Filled.PedalBike,
    "pending" to Icons.Filled.Pending,
    "pending_actions" to Icons.Filled.PendingActions,
    "pentagon" to Icons.Filled.Pentagon,
    "people" to Icons.Filled.People,
    "people_alt" to Icons.Filled.PeopleAlt,
    "people_outline" to Icons.Filled.PeopleOutline,
    "percent" to Icons.Filled.Percent,
    "perm_camera_mic" to Icons.Filled.PermCameraMic,
    "perm_contact_calendar" to Icons.Filled.PermContactCalendar,
    "perm_data_setting" to Icons.Filled.PermDataSetting,
    "perm_device_information" to Icons.Filled.PermDeviceInformation,
    "perm_identity" to Icons.Filled.PermIdentity,
    "perm_media" to Icons.Filled.PermMedia,
    "perm_phone_msg" to Icons.Filled.PermPhoneMsg,
    "perm_scan_wifi" to Icons.Filled.PermScanWifi,
    "person_2" to Icons.Filled.Person2,
    "person_3" to Icons.Filled.Person3,
    "person_4" to Icons.Filled.Person4,
    "person_add" to Icons.Filled.PersonAdd,
    "person_add_alt" to Icons.Filled.PersonAddAlt,
    "person_add_alt_1" to Icons.Filled.PersonAddAlt1,
    "person_add_disabled" to Icons.Filled.PersonAddDisabled,
    "personal_injury" to Icons.Filled.PersonalInjury,
    "personal_video" to Icons.Filled.PersonalVideo,
    "person_off" to Icons.Filled.PersonOff,
    "person_outline" to Icons.Filled.PersonOutline,
    "person_pin" to Icons.Filled.PersonPin,
    "person_pin_circle" to Icons.Filled.PersonPinCircle,
    "person_remove" to Icons.Filled.PersonRemove,
    "person_remove_alt_1" to Icons.Filled.PersonRemoveAlt1,
    "person_search" to Icons.Filled.PersonSearch,
    "pest_control" to Icons.Filled.PestControl,
    "pest_control_rodent" to Icons.Filled.PestControlRodent,
    "phishing" to Icons.Filled.Phishing,
    "phone_android" to Icons.Filled.PhoneAndroid,
    "phone_bluetooth_speaker" to Icons.Filled.PhoneBluetoothSpeaker,
    "phone_callback" to Icons.Filled.PhoneCallback,
    "phone_disabled" to Icons.Filled.PhoneDisabled,
    "phone_enabled" to Icons.Filled.PhoneEnabled,
    "phone_forwarded" to Icons.Filled.PhoneForwarded,
    "phone_in_talk" to Icons.Filled.PhoneInTalk,
    "phone_iphone" to Icons.Filled.PhoneIphone,
    "phonelink" to Icons.Filled.Phonelink,
    "phonelink_erase" to Icons.Filled.PhonelinkErase,
    "phonelink_lock" to Icons.Filled.PhonelinkLock,
    "phonelink_off" to Icons.Filled.PhonelinkOff,
    "phonelink_ring" to Icons.Filled.PhonelinkRing,
    "phonelink_setup" to Icons.Filled.PhonelinkSetup,
    "phone_locked" to Icons.Filled.PhoneLocked,
    "phone_missed" to Icons.Filled.PhoneMissed,
    "phone_paused" to Icons.Filled.PhonePaused,
    "photo_album" to Icons.Filled.PhotoAlbum,
    "photo_camera" to Icons.Filled.PhotoCamera,
    "photo_camera_back" to Icons.Filled.PhotoCameraBack,
    "photo_camera_front" to Icons.Filled.PhotoCameraFront,
    "photo_filter" to Icons.Filled.PhotoFilter,
    "photo_library" to Icons.Filled.PhotoLibrary,
    "photo_size_select_actual" to Icons.Filled.PhotoSizeSelectActual,
    "photo_size_select_large" to Icons.Filled.PhotoSizeSelectLarge,
    "photo_size_select_small" to Icons.Filled.PhotoSizeSelectSmall,
    "php" to Icons.Filled.Php,
    "piano" to Icons.Filled.Piano,
    "piano_off" to Icons.Filled.PianoOff,
    "picture_as_pdf" to Icons.Filled.PictureAsPdf,
    "picture_in_picture" to Icons.Filled.PictureInPicture,
    "picture_in_picture_alt" to Icons.Filled.PictureInPictureAlt,
    "pie_chart" to Icons.Filled.PieChart,
    "pie_chart_outline" to Icons.Filled.PieChartOutline,
    "pin" to Icons.Filled.Pin,
    "pinch" to Icons.Filled.Pinch,
    "pin_drop" to Icons.Filled.PinDrop,
    "pin_end" to Icons.Filled.PinEnd,
    "pin_invoke" to Icons.Filled.PinInvoke,
    "pivot_table_chart" to Icons.Filled.PivotTableChart,
    "pix" to Icons.Filled.Pix,
    "plagiarism" to Icons.Filled.Plagiarism,
    "play_circle" to Icons.Filled.PlayCircle,
    "play_circle_filled" to Icons.Filled.PlayCircleFilled,
    "play_circle_outline" to Icons.Filled.PlayCircleOutline,
    "play_disabled" to Icons.Filled.PlayDisabled,
    "play_for_work" to Icons.Filled.PlayForWork,
    "play_lesson" to Icons.Filled.PlayLesson,
    "playlist_add" to Icons.Filled.PlaylistAdd,
    "playlist_add_check" to Icons.Filled.PlaylistAddCheck,
    "playlist_add_check_circle" to Icons.Filled.PlaylistAddCheckCircle,
    "playlist_add_circle" to Icons.Filled.PlaylistAddCircle,
    "playlist_play" to Icons.Filled.PlaylistPlay,
    "playlist_remove" to Icons.Filled.PlaylistRemove,
    "plumbing" to Icons.Filled.Plumbing,
    "plus_one" to Icons.Filled.PlusOne,
    "podcasts" to Icons.Filled.Podcasts,
    "point_of_sale" to Icons.Filled.PointOfSale,
    "policy" to Icons.Filled.Policy,
    "poll" to Icons.Filled.Poll,
    "polyline" to Icons.Filled.Polyline,
    "polymer" to Icons.Filled.Polymer,
    "pool" to Icons.Filled.Pool,
    "portable_wifi_off" to Icons.Filled.PortableWifiOff,
    "portrait" to Icons.Filled.Portrait,
    "post_add" to Icons.Filled.PostAdd,
    "power" to Icons.Filled.Power,
    "power_input" to Icons.Filled.PowerInput,
    "power_off" to Icons.Filled.PowerOff,
    "power_settings_new" to Icons.Filled.PowerSettingsNew,
    "precision_manufacturing" to Icons.Filled.PrecisionManufacturing,
    "pregnant_woman" to Icons.Filled.PregnantWoman,
    "present_to_all" to Icons.Filled.PresentToAll,
    "preview" to Icons.Filled.Preview,
    "price_change" to Icons.Filled.PriceChange,
    "price_check" to Icons.Filled.PriceCheck,
    "print" to Icons.Filled.Print,
    "print_disabled" to Icons.Filled.PrintDisabled,
    "priority_high" to Icons.Filled.PriorityHigh,
    "privacy_tip" to Icons.Filled.PrivacyTip,
    "private_connectivity" to Icons.Filled.PrivateConnectivity,
    "production_quantity_limits" to Icons.Filled.ProductionQuantityLimits,
    "propane" to Icons.Filled.Propane,
    "propane_tank" to Icons.Filled.PropaneTank,
    "psychology_alt" to Icons.Filled.PsychologyAlt,
    "public" to Icons.Filled.Public,
    "public_off" to Icons.Filled.PublicOff,
    "publish" to Icons.Filled.Publish,
    "published_with_changes" to Icons.Filled.PublishedWithChanges,
    "punch_clock" to Icons.Filled.PunchClock,
    "push_pin" to Icons.Filled.PushPin,
    "qr_code" to Icons.Filled.QrCode,
    "qr_code_2" to Icons.Filled.QrCode2,
    "qr_code_scanner" to Icons.Filled.QrCodeScanner,
    "query_builder" to Icons.Filled.QueryBuilder,
    "query_stats" to Icons.Filled.QueryStats,
    "question_answer" to Icons.Filled.QuestionAnswer,
    "question_mark" to Icons.Filled.QuestionMark,
    "queue" to Icons.Filled.Queue,
    "queue_music" to Icons.Filled.QueueMusic,
    "queue_play_next" to Icons.Filled.QueuePlayNext,
    "quickreply" to Icons.Filled.Quickreply,
    "quiz" to Icons.Filled.Quiz,
    "radar" to Icons.Filled.Radar,
    "radio" to Icons.Filled.Radio,
    "radio_button_checked" to Icons.Filled.RadioButtonChecked,
    "radio_button_unchecked" to Icons.Filled.RadioButtonUnchecked,
    "railway_alert" to Icons.Filled.RailwayAlert,
    "ramen_dining" to Icons.Filled.RamenDining,
    "ramp_left" to Icons.Filled.RampLeft,
    "ramp_right" to Icons.Filled.RampRight,
    "rate_review" to Icons.Filled.RateReview,
    "raw_off" to Icons.Filled.RawOff,
    "raw_on" to Icons.Filled.RawOn,
    "read_more" to Icons.Filled.ReadMore,
    "real_estate_agent" to Icons.Filled.RealEstateAgent,
    "receipt" to Icons.Filled.Receipt,
    "receipt_long" to Icons.Filled.ReceiptLong,
    "recent_actors" to Icons.Filled.RecentActors,
    "recommend" to Icons.Filled.Recommend,
    "record_voice_over" to Icons.Filled.RecordVoiceOver,
    "rectangle" to Icons.Filled.Rectangle,
    "recycling" to Icons.Filled.Recycling,
    "redeem" to Icons.Filled.Redeem,
    "redo" to Icons.Filled.Redo,
    "reduce_capacity" to Icons.Filled.ReduceCapacity,
    "remember_me" to Icons.Filled.RememberMe,
    "remove" to Icons.Filled.Remove,
    "remove_circle" to Icons.Filled.RemoveCircle,
    "remove_circle_outline" to Icons.Filled.RemoveCircleOutline,
    "remove_done" to Icons.Filled.RemoveDone,
    "remove_from_queue" to Icons.Filled.RemoveFromQueue,
    "remove_moderator" to Icons.Filled.RemoveModerator,
    "remove_red_eye" to Icons.Filled.RemoveRedEye,
    "remove_road" to Icons.Filled.RemoveRoad,
    "remove_shopping_cart" to Icons.Filled.RemoveShoppingCart,
    "reorder" to Icons.Filled.Reorder,
    "repartition" to Icons.Filled.Repartition,
    "repeat" to Icons.Filled.Repeat,
    "repeat_on" to Icons.Filled.RepeatOn,
    "repeat_one" to Icons.Filled.RepeatOne,
    "repeat_one_on" to Icons.Filled.RepeatOneOn,
    "replay" to Icons.Filled.Replay,
    "replay_10" to Icons.Filled.Replay10,
    "replay_30" to Icons.Filled.Replay30,
    "replay_5" to Icons.Filled.Replay5,
    "replay_circle_filled" to Icons.Filled.ReplayCircleFilled,
    "reply" to Icons.Filled.Reply,
    "reply_all" to Icons.Filled.ReplyAll,
    "report" to Icons.Filled.Report,
    "report_gmailerrorred" to Icons.Filled.ReportGmailerrorred,
    "report_off" to Icons.Filled.ReportOff,
    "report_problem" to Icons.Filled.ReportProblem,
    "request_page" to Icons.Filled.RequestPage,
    "request_quote" to Icons.Filled.RequestQuote,
    "reset_tv" to Icons.Filled.ResetTv,
    "restart_alt" to Icons.Filled.RestartAlt,
    "restaurant_menu" to Icons.Filled.RestaurantMenu,
    "restore" to Icons.Filled.Restore,
    "restore_from_trash" to Icons.Filled.RestoreFromTrash,
    "restore_page" to Icons.Filled.RestorePage,
    "reviews" to Icons.Filled.Reviews,
    "rice_bowl" to Icons.Filled.RiceBowl,
    "ring_volume" to Icons.Filled.RingVolume,
    "r_mobiledata" to Icons.Filled.RMobiledata,
    "rocket_launch" to Icons.Filled.RocketLaunch,
    "roller_shades" to Icons.Filled.RollerShades,
    "roller_shades_closed" to Icons.Filled.RollerShadesClosed,
    "roller_skating" to Icons.Filled.RollerSkating,
    "roofing" to Icons.Filled.Roofing,
    "room" to Icons.Filled.Room,
    "room_preferences" to Icons.Filled.RoomPreferences,
    "room_service" to Icons.Filled.RoomService,
    "rotate_90_degrees_ccw" to Icons.Filled.Rotate90DegreesCcw,
    "rotate_90_degrees_cw" to Icons.Filled.Rotate90DegreesCw,
    "rotate_left" to Icons.Filled.RotateLeft,
    "rotate_right" to Icons.Filled.RotateRight,
    "roundabout_left" to Icons.Filled.RoundaboutLeft,
    "roundabout_right" to Icons.Filled.RoundaboutRight,
    "rounded_corner" to Icons.Filled.RoundedCorner,
    "route" to Icons.Filled.Route,
    "router" to Icons.Filled.Router,
    "rowing" to Icons.Filled.Rowing,
    "rss_feed" to Icons.Filled.RssFeed,
    "rsvp" to Icons.Filled.Rsvp,
    "rtt" to Icons.Filled.Rtt,
    "rule" to Icons.Filled.Rule,
    "rule_folder" to Icons.Filled.RuleFolder,
    "run_circle" to Icons.Filled.RunCircle,
    "running_with_errors" to Icons.Filled.RunningWithErrors,
    "rv_hookup" to Icons.Filled.RvHookup,
    "safety_check" to Icons.Filled.SafetyCheck,
    "safety_divider" to Icons.Filled.SafetyDivider,
    "sailing" to Icons.Filled.Sailing,
    "sanitizer" to Icons.Filled.Sanitizer,
    "satellite" to Icons.Filled.Satellite,
    "satellite_alt" to Icons.Filled.SatelliteAlt,
    "save" to Icons.Filled.Save,
    "save_alt" to Icons.Filled.SaveAlt,
    "save_as" to Icons.Filled.SaveAs,
    "saved_search" to Icons.Filled.SavedSearch,
    "savings" to Icons.Filled.Savings,
    "scale" to Icons.Filled.Scale,
    "scanner" to Icons.Filled.Scanner,
    "scatter_plot" to Icons.Filled.ScatterPlot,
    "schedule" to Icons.Filled.Schedule,
    "schedule_send" to Icons.Filled.ScheduleSend,
    "schema" to Icons.Filled.Schema,
    "score" to Icons.Filled.Score,
    "scoreboard" to Icons.Filled.Scoreboard,
    "screen_lock_landscape" to Icons.Filled.ScreenLockLandscape,
    "screen_lock_portrait" to Icons.Filled.ScreenLockPortrait,
    "screen_lock_rotation" to Icons.Filled.ScreenLockRotation,
    "screen_rotation" to Icons.Filled.ScreenRotation,
    "screen_rotation_alt" to Icons.Filled.ScreenRotationAlt,
    "screen_search_desktop" to Icons.Filled.ScreenSearchDesktop,
    "screen_share" to Icons.Filled.ScreenShare,
    "screenshot" to Icons.Filled.Screenshot,
    "screenshot_monitor" to Icons.Filled.ScreenshotMonitor,
    "scuba_diving" to Icons.Filled.ScubaDiving,
    "sd" to Icons.Filled.Sd,
    "sd_card" to Icons.Filled.SdCard,
    "sd_card_alert" to Icons.Filled.SdCardAlert,
    "sd_storage" to Icons.Filled.SdStorage,
    "search_off" to Icons.Filled.SearchOff,
    "security" to Icons.Filled.Security,
    "security_update" to Icons.Filled.SecurityUpdate,
    "security_update_good" to Icons.Filled.SecurityUpdateGood,
    "security_update_warning" to Icons.Filled.SecurityUpdateWarning,
    "segment" to Icons.Filled.Segment,
    "select_all" to Icons.Filled.SelectAll,
    "self_improvement" to Icons.Filled.SelfImprovement,
    "sell" to Icons.Filled.Sell,
    "send_and_archive" to Icons.Filled.SendAndArchive,
    "send_time_extension" to Icons.Filled.SendTimeExtension,
    "send_to_mobile" to Icons.Filled.SendToMobile,
    "sensor_door" to Icons.Filled.SensorDoor,
    "sensor_occupied" to Icons.Filled.SensorOccupied,
    "sensors" to Icons.Filled.Sensors,
    "sensors_off" to Icons.Filled.SensorsOff,
    "sensor_window" to Icons.Filled.SensorWindow,
    "sentiment_dissatisfied" to Icons.Filled.SentimentDissatisfied,
    "sentiment_neutral" to Icons.Filled.SentimentNeutral,
    "sentiment_satisfied" to Icons.Filled.SentimentSatisfied,
    "sentiment_satisfied_alt" to Icons.Filled.SentimentSatisfiedAlt,
    "sentiment_very_dissatisfied" to Icons.Filled.SentimentVeryDissatisfied,
    "sentiment_very_satisfied" to Icons.Filled.SentimentVerySatisfied,
    "set_meal" to Icons.Filled.SetMeal,
    "settings_accessibility" to Icons.Filled.SettingsAccessibility,
    "settings_applications" to Icons.Filled.SettingsApplications,
    "settings_backup_restore" to Icons.Filled.SettingsBackupRestore,
    "settings_bluetooth" to Icons.Filled.SettingsBluetooth,
    "settings_brightness" to Icons.Filled.SettingsBrightness,
    "settings_cell" to Icons.Filled.SettingsCell,
    "settings_ethernet" to Icons.Filled.SettingsEthernet,
    "settings_input_antenna" to Icons.Filled.SettingsInputAntenna,
    "settings_input_component" to Icons.Filled.SettingsInputComponent,
    "settings_input_composite" to Icons.Filled.SettingsInputComposite,
    "settings_input_hdmi" to Icons.Filled.SettingsInputHdmi,
    "settings_input_svideo" to Icons.Filled.SettingsInputSvideo,
    "settings_overscan" to Icons.Filled.SettingsOverscan,
    "settings_phone" to Icons.Filled.SettingsPhone,
    "settings_power" to Icons.Filled.SettingsPower,
    "settings_remote" to Icons.Filled.SettingsRemote,
    "settings_suggest" to Icons.Filled.SettingsSuggest,
    "settings_system_daydream" to Icons.Filled.SettingsSystemDaydream,
    "settings_voice" to Icons.Filled.SettingsVoice,
    "severe_cold" to Icons.Filled.SevereCold,
    "shape_line" to Icons.Filled.ShapeLine,
    "share_location" to Icons.Filled.ShareLocation,
    "shield_moon" to Icons.Filled.ShieldMoon,
    "shop" to Icons.Filled.Shop,
    "shop_2" to Icons.Filled.Shop2,
    "shopping_bag" to Icons.Filled.ShoppingBag,
    "shopping_basket" to Icons.Filled.ShoppingBasket,
    "shopping_cart_checkout" to Icons.Filled.ShoppingCartCheckout,
    "shop_two" to Icons.Filled.ShopTwo,
    "shortcut" to Icons.Filled.Shortcut,
    "short_text" to Icons.Filled.ShortText,
    "show_chart" to Icons.Filled.ShowChart,
    "shower" to Icons.Filled.Shower,
    "shuffle" to Icons.Filled.Shuffle,
    "shuffle_on" to Icons.Filled.ShuffleOn,
    "shutter_speed" to Icons.Filled.ShutterSpeed,
    "sick" to Icons.Filled.Sick,
    "signal_cellular_0_bar" to Icons.Filled.SignalCellular0Bar,
    "signal_cellular_4_bar" to Icons.Filled.SignalCellular4Bar,
    "signal_cellular_alt" to Icons.Filled.SignalCellularAlt,
    "signal_cellular_alt_1_bar" to Icons.Filled.SignalCellularAlt1Bar,
    "signal_cellular_alt_2_bar" to Icons.Filled.SignalCellularAlt2Bar,
    "signal_cellular_connected_no_internet_0_bar" to Icons.Filled.SignalCellularConnectedNoInternet0Bar,
    "signal_cellular_connected_no_internet_4_bar" to Icons.Filled.SignalCellularConnectedNoInternet4Bar,
    "signal_cellular_nodata" to Icons.Filled.SignalCellularNodata,
    "signal_cellular_no_sim" to Icons.Filled.SignalCellularNoSim,
    "signal_cellular_null" to Icons.Filled.SignalCellularNull,
    "signal_cellular_off" to Icons.Filled.SignalCellularOff,
    "signal_wifi_0_bar" to Icons.Filled.SignalWifi0Bar,
    "signal_wifi_4_bar" to Icons.Filled.SignalWifi4Bar,
    "signal_wifi_4_bar_lock" to Icons.Filled.SignalWifi4BarLock,
    "signal_wifi_bad" to Icons.Filled.SignalWifiBad,
    "signal_wifi_connected_no_internet_4" to Icons.Filled.SignalWifiConnectedNoInternet4,
    "signal_wifi_off" to Icons.Filled.SignalWifiOff,
    "signal_wifi_statusbar_4_bar" to Icons.Filled.SignalWifiStatusbar4Bar,
    "signal_wifi_statusbar_connected_no_internet_4" to Icons.Filled.SignalWifiStatusbarConnectedNoInternet4,
    "signal_wifi_statusbar_null" to Icons.Filled.SignalWifiStatusbarNull,
    "sign_language" to Icons.Filled.SignLanguage,
    "signpost" to Icons.Filled.Signpost,
    "sim_card" to Icons.Filled.SimCard,
    "sim_card_alert" to Icons.Filled.SimCardAlert,
    "sim_card_download" to Icons.Filled.SimCardDownload,
    "single_bed" to Icons.Filled.SingleBed,
    "sip" to Icons.Filled.Sip,
    "skateboarding" to Icons.Filled.Skateboarding,
    "skip_next" to Icons.Filled.SkipNext,
    "skip_previous" to Icons.Filled.SkipPrevious,
    "sledding" to Icons.Filled.Sledding,
    "slideshow" to Icons.Filled.Slideshow,
    "slow_motion_video" to Icons.Filled.SlowMotionVideo,
    "smart_button" to Icons.Filled.SmartButton,
    "smart_display" to Icons.Filled.SmartDisplay,
    "smartphone" to Icons.Filled.Smartphone,
    "smart_screen" to Icons.Filled.SmartScreen,
    "smart_toy" to Icons.Filled.SmartToy,
    "smoke_free" to Icons.Filled.SmokeFree,
    "smoking_rooms" to Icons.Filled.SmokingRooms,
    "sms" to Icons.Filled.Sms,
    "sms_failed" to Icons.Filled.SmsFailed,
    "snippet_folder" to Icons.Filled.SnippetFolder,
    "snooze" to Icons.Filled.Snooze,
    "snowboarding" to Icons.Filled.Snowboarding,
    "snowmobile" to Icons.Filled.Snowmobile,
    "snowshoeing" to Icons.Filled.Snowshoeing,
    "soap" to Icons.Filled.Soap,
    "social_distance" to Icons.Filled.SocialDistance,
    "solar_power" to Icons.Filled.SolarPower,
    "sort" to Icons.Filled.Sort,
    "sort_by_alpha" to Icons.Filled.SortByAlpha,
    "sos" to Icons.Filled.Sos,
    "soup_kitchen" to Icons.Filled.SoupKitchen,
    "source" to Icons.Filled.Source,
    "south" to Icons.Filled.South,
    "south_america" to Icons.Filled.SouthAmerica,
    "south_east" to Icons.Filled.SouthEast,
    "south_west" to Icons.Filled.SouthWest,
    "spa" to Icons.Filled.Spa,
    "space_bar" to Icons.Filled.SpaceBar,
    "space_dashboard" to Icons.Filled.SpaceDashboard,
    "spatial_audio" to Icons.Filled.SpatialAudio,
    "spatial_audio_off" to Icons.Filled.SpatialAudioOff,
    "spatial_tracking" to Icons.Filled.SpatialTracking,
    "speaker" to Icons.Filled.Speaker,
    "speaker_group" to Icons.Filled.SpeakerGroup,
    "speaker_notes" to Icons.Filled.SpeakerNotes,
    "speaker_notes_off" to Icons.Filled.SpeakerNotesOff,
    "speaker_phone" to Icons.Filled.SpeakerPhone,
    "speed" to Icons.Filled.Speed,
    "spellcheck" to Icons.Filled.Spellcheck,
    "splitscreen" to Icons.Filled.Splitscreen,
    "spoke" to Icons.Filled.Spoke,
    "sports_bar" to Icons.Filled.SportsBar,
    "sports_baseball" to Icons.Filled.SportsBaseball,
    "sports_basketball" to Icons.Filled.SportsBasketball,
    "sports_cricket" to Icons.Filled.SportsCricket,
    "sports_esports" to Icons.Filled.SportsEsports,
    "sports_football" to Icons.Filled.SportsFootball,
    "sports_golf" to Icons.Filled.SportsGolf,
    "sports_gymnastics" to Icons.Filled.SportsGymnastics,
    "sports_handball" to Icons.Filled.SportsHandball,
    "sports_hockey" to Icons.Filled.SportsHockey,
    "sports_kabaddi" to Icons.Filled.SportsKabaddi,
    "sports_martial_arts" to Icons.Filled.SportsMartialArts,
    "sports_mma" to Icons.Filled.SportsMma,
    "sports_motorsports" to Icons.Filled.SportsMotorsports,
    "sports_rugby" to Icons.Filled.SportsRugby,
    "sports_score" to Icons.Filled.SportsScore,
    "sports_soccer" to Icons.Filled.SportsSoccer,
    "sports_tennis" to Icons.Filled.SportsTennis,
    "sports_volleyball" to Icons.Filled.SportsVolleyball,
    "square" to Icons.Filled.Square,
    "square_foot" to Icons.Filled.SquareFoot,
    "ssid_chart" to Icons.Filled.SsidChart,
    "stacked_bar_chart" to Icons.Filled.StackedBarChart,
    "stacked_line_chart" to Icons.Filled.StackedLineChart,
    "stadium" to Icons.Filled.Stadium,
    "stairs" to Icons.Filled.Stairs,
    "star_border" to Icons.Filled.StarBorder,
    "star_border_purple_500" to Icons.Filled.StarBorderPurple500,
    "star_half" to Icons.Filled.StarHalf,
    "star_outline" to Icons.Filled.StarOutline,
    "star_purple_500" to Icons.Filled.StarPurple500,
    "star_rate" to Icons.Filled.StarRate,
    "stars" to Icons.Filled.Stars,
    "start" to Icons.Filled.Start,
    "stay_current_landscape" to Icons.Filled.StayCurrentLandscape,
    "stay_current_portrait" to Icons.Filled.StayCurrentPortrait,
    "stay_primary_landscape" to Icons.Filled.StayPrimaryLandscape,
    "stay_primary_portrait" to Icons.Filled.StayPrimaryPortrait,
    "sticky_note_2" to Icons.Filled.StickyNote2,
    "stop" to Icons.Filled.Stop,
    "stop_circle" to Icons.Filled.StopCircle,
    "stop_screen_share" to Icons.Filled.StopScreenShare,
    "storage" to Icons.Filled.Storage,
    "store" to Icons.Filled.Store,
    "storefront" to Icons.Filled.Storefront,
    "store_mall_directory" to Icons.Filled.StoreMallDirectory,
    "storm" to Icons.Filled.Storm,
    "straight" to Icons.Filled.Straight,
    "straighten" to Icons.Filled.Straighten,
    "stream" to Icons.Filled.Stream,
    "streetview" to Icons.Filled.Streetview,
    "strikethrough_s" to Icons.Filled.StrikethroughS,
    "stroller" to Icons.Filled.Stroller,
    "style" to Icons.Filled.Style,
    "subdirectory_arrow_left" to Icons.Filled.SubdirectoryArrowLeft,
    "subdirectory_arrow_right" to Icons.Filled.SubdirectoryArrowRight,
    "subject" to Icons.Filled.Subject,
    "subscript" to Icons.Filled.Subscript,
    "subscriptions" to Icons.Filled.Subscriptions,
    "subtitles" to Icons.Filled.Subtitles,
    "subtitles_off" to Icons.Filled.SubtitlesOff,
    "subway" to Icons.Filled.Subway,
    "summarize" to Icons.Filled.Summarize,
    "superscript" to Icons.Filled.Superscript,
    "supervised_user_circle" to Icons.Filled.SupervisedUserCircle,
    "supervisor_account" to Icons.Filled.SupervisorAccount,
    "support" to Icons.Filled.Support,
    "support_agent" to Icons.Filled.SupportAgent,
    "surfing" to Icons.Filled.Surfing,
    "surround_sound" to Icons.Filled.SurroundSound,
    "swap_calls" to Icons.Filled.SwapCalls,
    "swap_horiz" to Icons.Filled.SwapHoriz,
    "swap_horizontal_circle" to Icons.Filled.SwapHorizontalCircle,
    "swap_vert" to Icons.Filled.SwapVert,
    "swap_vertical_circle" to Icons.Filled.SwapVerticalCircle,
    "swipe" to Icons.Filled.Swipe,
    "swipe_down" to Icons.Filled.SwipeDown,
    "swipe_down_alt" to Icons.Filled.SwipeDownAlt,
    "swipe_left" to Icons.Filled.SwipeLeft,
    "swipe_left_alt" to Icons.Filled.SwipeLeftAlt,
    "swipe_right" to Icons.Filled.SwipeRight,
    "swipe_right_alt" to Icons.Filled.SwipeRightAlt,
    "swipe_up" to Icons.Filled.SwipeUp,
    "swipe_up_alt" to Icons.Filled.SwipeUpAlt,
    "swipe_vertical" to Icons.Filled.SwipeVertical,
    "switch_access_shortcut" to Icons.Filled.SwitchAccessShortcut,
    "switch_access_shortcut_add" to Icons.Filled.SwitchAccessShortcutAdd,
    "switch_account" to Icons.Filled.SwitchAccount,
    "switch_camera" to Icons.Filled.SwitchCamera,
    "switch_left" to Icons.Filled.SwitchLeft,
    "switch_right" to Icons.Filled.SwitchRight,
    "switch_video" to Icons.Filled.SwitchVideo,
    "synagogue" to Icons.Filled.Synagogue,
    "sync" to Icons.Filled.Sync,
    "sync_alt" to Icons.Filled.SyncAlt,
    "sync_disabled" to Icons.Filled.SyncDisabled,
    "sync_lock" to Icons.Filled.SyncLock,
    "sync_problem" to Icons.Filled.SyncProblem,
    "system_security_update" to Icons.Filled.SystemSecurityUpdate,
    "system_security_update_good" to Icons.Filled.SystemSecurityUpdateGood,
    "system_security_update_warning" to Icons.Filled.SystemSecurityUpdateWarning,
    "system_update" to Icons.Filled.SystemUpdate,
    "system_update_alt" to Icons.Filled.SystemUpdateAlt,
    "tab" to Icons.Filled.Tab,
    "table_bar" to Icons.Filled.TableBar,
    "table_chart" to Icons.Filled.TableChart,
    "table_restaurant" to Icons.Filled.TableRestaurant,
    "table_rows" to Icons.Filled.TableRows,
    "tablet" to Icons.Filled.Tablet,
    "tablet_android" to Icons.Filled.TabletAndroid,
    "tablet_mac" to Icons.Filled.TabletMac,
    "table_view" to Icons.Filled.TableView,
    "tab_unselected" to Icons.Filled.TabUnselected,
    "tag" to Icons.Filled.Tag,
    "tag_faces" to Icons.Filled.TagFaces,
    "takeout_dining" to Icons.Filled.TakeoutDining,
    "tap_and_play" to Icons.Filled.TapAndPlay,
    "tapas" to Icons.Filled.Tapas,
    "task" to Icons.Filled.Task,
    "task_alt" to Icons.Filled.TaskAlt,
    "taxi_alert" to Icons.Filled.TaxiAlert,
    "temple_buddhist" to Icons.Filled.TempleBuddhist,
    "temple_hindu" to Icons.Filled.TempleHindu,
    "terminal" to Icons.Filled.Terminal,
    "terrain" to Icons.Filled.Terrain,
    "text_decrease" to Icons.Filled.TextDecrease,
    "text_fields" to Icons.Filled.TextFields,
    "text_format" to Icons.Filled.TextFormat,
    "text_increase" to Icons.Filled.TextIncrease,
    "text_rotate_up" to Icons.Filled.TextRotateUp,
    "text_rotate_vertical" to Icons.Filled.TextRotateVertical,
    "text_rotation_angledown" to Icons.Filled.TextRotationAngledown,
    "text_rotation_angleup" to Icons.Filled.TextRotationAngleup,
    "text_rotation_down" to Icons.Filled.TextRotationDown,
    "text_rotation_none" to Icons.Filled.TextRotationNone,
    "textsms" to Icons.Filled.Textsms,
    "text_snippet" to Icons.Filled.TextSnippet,
    "texture" to Icons.Filled.Texture,
    "theater_comedy" to Icons.Filled.TheaterComedy,
    "theaters" to Icons.Filled.Theaters,
    "thermostat" to Icons.Filled.Thermostat,
    "thermostat_auto" to Icons.Filled.ThermostatAuto,
    "thumb_down" to Icons.Filled.ThumbDown,
    "thumb_down_alt" to Icons.Filled.ThumbDownAlt,
    "thumb_down_off_alt" to Icons.Filled.ThumbDownOffAlt,
    "thumbs_up_down" to Icons.Filled.ThumbsUpDown,
    "thumb_up_alt" to Icons.Filled.ThumbUpAlt,
    "thumb_up_off_alt" to Icons.Filled.ThumbUpOffAlt,
    "thunderstorm" to Icons.Filled.Thunderstorm,
    "timelapse" to Icons.Filled.Timelapse,
    "timeline" to Icons.Filled.Timeline,
    "timer_10" to Icons.Filled.Timer10,
    "timer_10_select" to Icons.Filled.Timer10Select,
    "timer_3" to Icons.Filled.Timer3,
    "timer_3_select" to Icons.Filled.Timer3Select,
    "timer_off" to Icons.Filled.TimerOff,
    "time_to_leave" to Icons.Filled.TimeToLeave,
    "tips_and_updates" to Icons.Filled.TipsAndUpdates,
    "tire_repair" to Icons.Filled.TireRepair,
    "title" to Icons.Filled.Title,
    "toc" to Icons.Filled.Toc,
    "today" to Icons.Filled.Today,
    "toggle_off" to Icons.Filled.ToggleOff,
    "toggle_on" to Icons.Filled.ToggleOn,
    "token" to Icons.Filled.Token,
    "toll" to Icons.Filled.Toll,
    "tonality" to Icons.Filled.Tonality,
    "topic" to Icons.Filled.Topic,
    "tornado" to Icons.Filled.Tornado,
    "touch_app" to Icons.Filled.TouchApp,
    "tour" to Icons.Filled.Tour,
    "toys" to Icons.Filled.Toys,
    "track_changes" to Icons.Filled.TrackChanges,
    "traffic" to Icons.Filled.Traffic,
    "train" to Icons.Filled.Train,
    "tram" to Icons.Filled.Tram,
    "transcribe" to Icons.Filled.Transcribe,
    "transfer_within_a_station" to Icons.Filled.TransferWithinAStation,
    "transform" to Icons.Filled.Transform,
    "transgender" to Icons.Filled.Transgender,
    "transit_enterexit" to Icons.Filled.TransitEnterexit,
    "translate" to Icons.Filled.Translate,
    "travel_explore" to Icons.Filled.TravelExplore,
    "trending_down" to Icons.Filled.TrendingDown,
    "trending_flat" to Icons.Filled.TrendingFlat,
    "trending_up" to Icons.Filled.TrendingUp,
    "trip_origin" to Icons.Filled.TripOrigin,
    "troubleshoot" to Icons.Filled.Troubleshoot,
    "tsunami" to Icons.Filled.Tsunami,
    "tty" to Icons.Filled.Tty,
    "tune" to Icons.Filled.Tune,
    "tungsten" to Icons.Filled.Tungsten,
    "turned_in" to Icons.Filled.TurnedIn,
    "turned_in_not" to Icons.Filled.TurnedInNot,
    "turn_left" to Icons.Filled.TurnLeft,
    "turn_right" to Icons.Filled.TurnRight,
    "turn_sharp_left" to Icons.Filled.TurnSharpLeft,
    "turn_sharp_right" to Icons.Filled.TurnSharpRight,
    "turn_slight_left" to Icons.Filled.TurnSlightLeft,
    "turn_slight_right" to Icons.Filled.TurnSlightRight,
    "tv" to Icons.Filled.Tv,
    "tv_off" to Icons.Filled.TvOff,
    "two_wheeler" to Icons.Filled.TwoWheeler,
    "type_specimen" to Icons.Filled.TypeSpecimen,
    "umbrella" to Icons.Filled.Umbrella,
    "unarchive" to Icons.Filled.Unarchive,
    "undo" to Icons.Filled.Undo,
    "unfold_less" to Icons.Filled.UnfoldLess,
    "unfold_less_double" to Icons.Filled.UnfoldLessDouble,
    "unfold_more" to Icons.Filled.UnfoldMore,
    "unfold_more_double" to Icons.Filled.UnfoldMoreDouble,
    "unpublished" to Icons.Filled.Unpublished,
    "unsubscribe" to Icons.Filled.Unsubscribe,
    "upcoming" to Icons.Filled.Upcoming,
    "update" to Icons.Filled.Update,
    "update_disabled" to Icons.Filled.UpdateDisabled,
    "upgrade" to Icons.Filled.Upgrade,
    "upload" to Icons.Filled.Upload,
    "upload_file" to Icons.Filled.UploadFile,
    "usb" to Icons.Filled.Usb,
    "usb_off" to Icons.Filled.UsbOff,
    "u_turn_left" to Icons.Filled.UTurnLeft,
    "u_turn_right" to Icons.Filled.UTurnRight,
    "vaccines" to Icons.Filled.Vaccines,
    "vape_free" to Icons.Filled.VapeFree,
    "vaping_rooms" to Icons.Filled.VapingRooms,
    "verified" to Icons.Filled.Verified,
    "verified_user" to Icons.Filled.VerifiedUser,
    "vertical_align_bottom" to Icons.Filled.VerticalAlignBottom,
    "vertical_align_center" to Icons.Filled.VerticalAlignCenter,
    "vertical_align_top" to Icons.Filled.VerticalAlignTop,
    "vertical_distribute" to Icons.Filled.VerticalDistribute,
    "vertical_shades" to Icons.Filled.VerticalShades,
    "vertical_shades_closed" to Icons.Filled.VerticalShadesClosed,
    "vertical_split" to Icons.Filled.VerticalSplit,
    "vibration" to Icons.Filled.Vibration,
    "video_call" to Icons.Filled.VideoCall,
    "videocam" to Icons.Filled.Videocam,
    "video_camera_back" to Icons.Filled.VideoCameraBack,
    "video_camera_front" to Icons.Filled.VideoCameraFront,
    "videocam_off" to Icons.Filled.VideocamOff,
    "video_chat" to Icons.Filled.VideoChat,
    "video_file" to Icons.Filled.VideoFile,
    "videogame_asset" to Icons.Filled.VideogameAsset,
    "videogame_asset_off" to Icons.Filled.VideogameAssetOff,
    "video_label" to Icons.Filled.VideoLabel,
    "video_library" to Icons.Filled.VideoLibrary,
    "video_settings" to Icons.Filled.VideoSettings,
    "video_stable" to Icons.Filled.VideoStable,
    "view_agenda" to Icons.Filled.ViewAgenda,
    "view_array" to Icons.Filled.ViewArray,
    "view_carousel" to Icons.Filled.ViewCarousel,
    "view_column" to Icons.Filled.ViewColumn,
    "view_comfy" to Icons.Filled.ViewComfy,
    "view_comfy_alt" to Icons.Filled.ViewComfyAlt,
    "view_compact" to Icons.Filled.ViewCompact,
    "view_compact_alt" to Icons.Filled.ViewCompactAlt,
    "view_cozy" to Icons.Filled.ViewCozy,
    "view_day" to Icons.Filled.ViewDay,
    "view_headline" to Icons.Filled.ViewHeadline,
    "view_in_ar" to Icons.Filled.ViewInAr,
    "view_kanban" to Icons.Filled.ViewKanban,
    "view_list" to Icons.Filled.ViewList,
    "view_module" to Icons.Filled.ViewModule,
    "view_quilt" to Icons.Filled.ViewQuilt,
    "view_sidebar" to Icons.Filled.ViewSidebar,
    "view_stream" to Icons.Filled.ViewStream,
    "view_timeline" to Icons.Filled.ViewTimeline,
    "view_week" to Icons.Filled.ViewWeek,
    "vignette" to Icons.Filled.Vignette,
    "villa" to Icons.Filled.Villa,
    "visibility" to Icons.Filled.Visibility,
    "visibility_off" to Icons.Filled.VisibilityOff,
    "voice_chat" to Icons.Filled.VoiceChat,
    "voicemail" to Icons.Filled.Voicemail,
    "voice_over_off" to Icons.Filled.VoiceOverOff,
    "volcano" to Icons.Filled.Volcano,
    "volume_down" to Icons.Filled.VolumeDown,
    "volume_mute" to Icons.Filled.VolumeMute,
    "volume_off" to Icons.Filled.VolumeOff,
    "volume_up" to Icons.Filled.VolumeUp,
    "volunteer_activism" to Icons.Filled.VolunteerActivism,
    "vpn_key" to Icons.Filled.VpnKey,
    "vpn_key_off" to Icons.Filled.VpnKeyOff,
    "vpn_lock" to Icons.Filled.VpnLock,
    "vrpano" to Icons.Filled.Vrpano,
    "wallet" to Icons.Filled.Wallet,
    "wallpaper" to Icons.Filled.Wallpaper,
    "warehouse" to Icons.Filled.Warehouse,
    "warning_amber" to Icons.Filled.WarningAmber,
    "wash" to Icons.Filled.Wash,
    "watch" to Icons.Filled.Watch,
    "watch_later" to Icons.Filled.WatchLater,
    "watch_off" to Icons.Filled.WatchOff,
    "water" to Icons.Filled.Water,
    "water_damage" to Icons.Filled.WaterDamage,
    "water_drop" to Icons.Filled.WaterDrop,
    "waterfall_chart" to Icons.Filled.WaterfallChart,
    "waves" to Icons.Filled.Waves,
    "waving_hand" to Icons.Filled.WavingHand,
    "wb_auto" to Icons.Filled.WbAuto,
    "wb_cloudy" to Icons.Filled.WbCloudy,
    "wb_incandescent" to Icons.Filled.WbIncandescent,
    "wb_iridescent" to Icons.Filled.WbIridescent,
    "wb_shade" to Icons.Filled.WbShade,
    "wb_sunny" to Icons.Filled.WbSunny,
    "wb_twilight" to Icons.Filled.WbTwilight,
    "wc" to Icons.Filled.Wc,
    "web" to Icons.Filled.Web,
    "web_asset" to Icons.Filled.WebAsset,
    "web_asset_off" to Icons.Filled.WebAssetOff,
    "webhook" to Icons.Filled.Webhook,
    "web_stories" to Icons.Filled.WebStories,
    "weekend" to Icons.Filled.Weekend,
    "west" to Icons.Filled.West,
    "whatsapp" to Icons.Filled.Whatsapp,
    "whatshot" to Icons.Filled.Whatshot,
    "wheelchair_pickup" to Icons.Filled.WheelchairPickup,
    "where_to_vote" to Icons.Filled.WhereToVote,
    "widgets" to Icons.Filled.Widgets,
    "width_full" to Icons.Filled.WidthFull,
    "width_normal" to Icons.Filled.WidthNormal,
    "width_wide" to Icons.Filled.WidthWide,
    "wifi_1_bar" to Icons.Filled.Wifi1Bar,
    "wifi_2_bar" to Icons.Filled.Wifi2Bar,
    "wifi_calling" to Icons.Filled.WifiCalling,
    "wifi_calling_3" to Icons.Filled.WifiCalling3,
    "wifi_channel" to Icons.Filled.WifiChannel,
    "wifi_find" to Icons.Filled.WifiFind,
    "wifi_lock" to Icons.Filled.WifiLock,
    "wifi_off" to Icons.Filled.WifiOff,
    "wifi_password" to Icons.Filled.WifiPassword,
    "wifi_protected_setup" to Icons.Filled.WifiProtectedSetup,
    "wifi_tethering" to Icons.Filled.WifiTethering,
    "wifi_tethering_error" to Icons.Filled.WifiTetheringError,
    "wifi_tethering_error_rounded" to Icons.Filled.WifiTetheringErrorRounded,
    "wifi_tethering_off" to Icons.Filled.WifiTetheringOff,
    "window" to Icons.Filled.Window,
    "wind_power" to Icons.Filled.WindPower,
    "wine_bar" to Icons.Filled.WineBar,
    "woman" to Icons.Filled.Woman,
    "woman_2" to Icons.Filled.Woman2,
    "work_history" to Icons.Filled.WorkHistory,
    "work_off" to Icons.Filled.WorkOff,
    "work_outline" to Icons.Filled.WorkOutline,
    "workspace_premium" to Icons.Filled.WorkspacePremium,
    "workspaces" to Icons.Filled.Workspaces,
    "wrap_text" to Icons.Filled.WrapText,
    "wrong_location" to Icons.Filled.WrongLocation,
    "wysiwyg" to Icons.Filled.Wysiwyg,
    "yard" to Icons.Filled.Yard,
    "youtube_searched_for" to Icons.Filled.YoutubeSearchedFor,
    "zoom_in" to Icons.Filled.ZoomIn,
    "zoom_in_map" to Icons.Filled.ZoomInMap,
    "zoom_out" to Icons.Filled.ZoomOut,
    "zoom_out_map" to Icons.Filled.ZoomOutMap,
)

/**
 * Filters [ICON_MAP]'s entries by a plain, case-insensitive substring match on the snake_case key.
 *
 * A blank [query] returns every entry unchanged, in [ICON_MAP]'s own insertion order. A non-blank
 * [query] returns only entries whose key contains it (case-insensitive) — no alias/synonym layer,
 * no fuzzy matcher, no ranking pass (D-01, ICON-01 milestone research vocabulary spot-check).
 *
 * `internal`, not `public`: kept out of the AAR's public API surface while remaining visible to this
 * module's own unit tests. Not `@Composable`, so the CATALOG-03 drift guard does not scan it.
 */
internal fun filterIconEntries(query: String): List<Map.Entry<String, ImageVector>> {
    if (query.isBlank()) return ICON_MAP.entries.toList()
    return ICON_MAP.entries.filter { it.key.contains(query, ignoreCase = true) }
}

/**
 * A scrollable grid picker over [ICON_MAP], with an internal live search filter above the grid.
 *
 * @param selectedIcon The currently selected [ICON_MAP] key; its cell is highlighted with the
 *   [MaterialTheme.colorScheme.primaryContainer] tint when it is present in the filtered set.
 * @param onIconSelected Invoked with an [ICON_MAP] key when its cell is tapped.
 * @param modifier Applied to the outer [Column] (search row + grid).
 * @param showIndices Gallery-only debug flag; renders a numeric badge on each cell indicating its
 *   position within the currently-rendered (i.e. filtered) list. Defaults to `false` in production.
 *
 * Search behavior: the field above the grid (labelled "Search icons") filters [ICON_MAP] live via
 * [filterIconEntries] — a plain, case-insensitive substring match on the snake_case key, with no
 * alias layer (D-01). An empty query shows the full grid; a query matching zero entries shows a
 * defined empty state in place of the grid. The filter recomputes synchronously on every keystroke
 * (no `remember`/`derivedStateOf` memoization) because a sub-millisecond in-memory scan over
 * [ICON_MAP]'s ~2,038 short keys does not warrant it; that memoization is a documented backstop, to
 * be added only if a future device pass observes real jank.
 */
@Composable
fun IconPickerGrid(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showIndices: Boolean = false
) {
    var query by remember { mutableStateOf("") }
    val entries = filterIconEntries(query)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ClearableTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search icons") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("icon_search_field")
            )
        }

        Box(
            modifier = Modifier
                .heightIn(max = 320.dp)
                .testTag("icon_search_grid")
        ) {
            LazyVerticalGrid(columns = GridCells.Fixed(5)) {
                itemsIndexed(entries, key = { _, e -> e.key }) { index, (name, vector) ->
                    val isSelected = name == selectedIcon
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            IconButton(
                                onClick = { onIconSelected(name) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = name
                                )
                            }
                        }
                        if (showIndices) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.inverseSurface)
                                    .padding(horizontal = 4.dp)
                                    .testTag("icon_index_badge")
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
