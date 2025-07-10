from datetime import datetime
from urllib.parse import quote

import streamlit as st

from app.pages.photos_recette import photo_viewer
from src.crud.recettes import get_recette_by_id
from src.db import get_db_session
from src.model import Convive, Execution, FeedbackExecution


def main():
    st.set_page_config(page_title="Détail recette", page_icon="📖")

    params = st.query_params
    recette_id = params.get("recette_id")

    if not recette_id:
        st.error("Aucune recette sélectionnée.")
        return

    with get_db_session() as session:
        recette = get_recette_by_id(session, recette_id)
    if not recette:
        st.error("Recette introuvable.")
        return

    st.title(recette.nom)

    # 📸 Affichage des photos
    st.subheader("📷 Photos")
    photo_viewer(recette)

    # Tags
    st.markdown("### 🏷️ Catégories")
    st.write(", ".join([c.nom for c in recette.categories]))

    # Ingrédients
    st.markdown("### 🧂 Ingrédients")
    for ing in recette.ingredients:
        ligne = f"- {ing.nom} : {ing.quantite} {ing.unite or ''}"
        if ing.indispensable:
            ligne += " (🟢 indispensable)"
        elif ing.alternatives:
            ligne += f" (🔄 alternatives : {ing.alternatives})"
        st.markdown(ligne)

    # Étapes
    st.markdown("### 📝 Étapes")
    for etape in sorted(recette.etapes, key=lambda e: e.ordre):
        st.markdown(f"**Étape {etape.ordre}** : {etape.description}")

    # Source
    st.markdown("### 📚 Source")
    if recette.source.type == "homemade":
        st.write("Recette maison")
    elif recette.source.type == "url":
        st.write(f"[Lien source]({recette.source.url})")
    elif recette.source.type == "book":
        st.write(f"{recette.source.book_title} — {recette.source.book_authors}")

    # Lien vers la page de gestion des photos
    st.markdown("### 🖼️ Voir les photos associées")
    url_photos = f"./photos_recette?recette_id={quote(str(recette.id))}"
    st.markdown(f"[📷 Gérer les photos]({url_photos})")

    # 🕓 Historique des réalisations
    st.subheader("🕓 Réalisations passées")

    executions: list[Execution] = sorted(recette.executions, key=lambda e: e.date_execution, reverse=True)
    if not executions:
        st.info("Cette recette n’a pas encore été cuisinée.")
    else:
        for exec in executions:
            if not isinstance(exec, Execution):
                continue
            st.markdown(f"**📅 {exec.date_execution.strftime('%d/%m/%Y')}**")
            for fb in exec.feedbacks:
                emoji = {"aimé": "✅", "partiellement": "🟡", "rien mangé": "🔴"}.get(fb.statut, "❓")
                st.markdown(f"- {emoji} {fb.convive.nom}")
            st.markdown("---")

    if st.button("🍽️ Je l’ai cuisinée !"):
        st.session_state["ajout_execution"] = True

    if st.session_state.get("ajout_execution"):
        with st.form("form_execution"):

            date = st.date_input("Date", value=datetime.today())
            convives = session.query(Convive).order_by(Convive.nom).all()
            selections = {}

            for c in convives:
                statut = st.radio(
                    f"{c.nom} ({c.groupe or 'autre'})",
                    ["aimé", "partiellement", "rien mangé"],
                    key=f"convive_{c.id}",
                    horizontal=True,
                )
                selections[c.id] = statut

            submitted = st.form_submit_button("Enregistrer")

            if submitted:
                # Enregistrer l'exécution
                exec = Execution(recette_id=recette.id, date_execution=date)
                for cid, statut in selections.items():
                    exec.feedbacks.append(FeedbackExecution(convive_id=cid, statut=statut))
                session.add(exec)
                session.commit()

                st.success("Exécution enregistrée !")
                st.session_state["ajout_execution"] = False


if __name__ == "__main__":
    main()
