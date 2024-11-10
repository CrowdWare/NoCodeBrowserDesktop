Page {
	padding: "8"
	title: "NoCodeBrowser"

	Column {
		padding: "8"
						
		Markdown {
			color: "#4C9BD9"
			text: "# Bienvenue"
  		}
  		Markdown { 
      		fontSize: 16
			text :"
				Nous sommes ravis que vous utilisiez la solution NoCode.
				Veuillez noter qu'il s'agit encore d'une version précoce, qui pourrait contenir quelques erreurs.
				Le contenu de cette application a été créé avec **NoCodeDesigner**, une application pour ordinateur de bureau."
		}
		Spacer { amount: 16 }
		Markdown {
			color: "#4C9BD9"
			text: "### NoCodeDesigner"
 		}
		Spacer { amount: 8 }
		Image { src: "desktop.png" }
		Spacer { amount: 16 }
		Markdown {
			fontSize: 16
			text: "Commencez maintenant, créez un E-Book ou même une application, et créez du contenu pour les autres, qui vous aidera à vous rapprocher de vos rêves."}

		Spacer { weight: 1 }
		Button { label: "À propos de NoCode" link: "page:app.about"}
	}
}