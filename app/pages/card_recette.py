from datetime import date, datetime
from urllib.parse import quote

import streamlit as st
from sqlalchemy import func
from streamlit_tags import st_tags

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
                if fb.convive.groupe:
                    st.markdown(f"- {emoji} {fb.convive.nom} ({fb.convive.groupe}) → {fb.statut}")
                else:
                    st.markdown(f"- {emoji} {fb.convive.nom} → {fb.statut}")
        st.markdown("---")

    if st.button("🍽️ Je l’ai cuisinée !"):
        st.session_state["ajout_execution"] = True

    if st.session_state.get("ajout_execution"):

        date_selected = st.date_input("Date", value=datetime.today())
        convives = session.query(Convive).order_by(Convive.nom).all()
        selections = {}

        # Nouveau champ pour ajouter un convive
        # new_convive_name = st.text_input("Ajouter un·e convive (laissez vide si aucun·e à ajouter)")
        new_convive_name = st_tags(
            label="Créer/modifier un·e convive (laissez vide si aucun·e à créer/modifier)",
            text="Press enter to add (only one)",
            suggestions=[c.nom for c in convives],
            maxtags=1,
            key="new_convive_name",
        )

        # new_convive_group = st.text_input("Groupe du convive (optionnel)")
        if new_convive_name:
            st.info(f"Convive à ajouter/modifier : {new_convive_name[0]}")
            st.info([c.nom for c in convives])
            st.info([c.groupe for c in convives if c.nom in new_convive_name])
            new_convive_group = st_tags(
                label="Groupe du convive (optionnel)",
                text="Press enter to add (only one)",
                value=[c.groupe for c in convives if c.nom in new_convive_name],
                suggestions=[c.groupe for c in convives if c.groupe],
                maxtags=1,
                key="new_convive_group",
            )

            if st.button("Enregistrer le convive"):
                from sqlalchemy.orm import Session

                def get_or_create_convive(session: Session, nom: str, groupe: str | None = None) -> Convive:
                    convive = session.query(Convive).filter_by(nom=nom).first()
                    if convive is None:
                        if groupe is None or groupe.strip() == "" or groupe.strip() == "Aucun":
                            groupe = None
                        convive = Convive(nom=nom, groupe=groupe)
                        session.add(convive)
                        session.commit()
                    else:
                        if groupe is not None and groupe.strip() != "":
                            convive.groupe = groupe.strip()
                            session.commit()
                    return convive

                with get_db_session() as session:
                    # Ajouter le nouveau convive s’il y en a un
                    if new_convive_name[0] and new_convive_name[0].strip():
                        nouveau = get_or_create_convive(
                            session, nom=new_convive_name[0].strip(), groupe=new_convive_group[0].strip() or None
                        )
                        session.add(nouveau)
                        session.commit()

                st.rerun()

        recette_execution_convive_names = st.multiselect(
            "Sélectionner un·e convive",
            options=[f"{c.nom} ({c.groupe})" if c.groupe else f"{c.nom}" for c in convives],
            key="recette_execution_convive_names",
        )

        for c in convives:
            if f"{c.nom} ({c.groupe})" in recette_execution_convive_names or c.nom in recette_execution_convive_names:
                statut = st.radio(
                    f"{c.nom} ({c.groupe or ''})" if c.groupe else str(c.nom),
                    ["aimé", "partiellement", "rien mangé"],
                    key=f"convive_{c.id}",
                    horizontal=True,
                )
                selections[c.id] = statut

        if st.button("Enregistrer l’exécution"):
            st.session_state["save_execution"] = True

        if st.session_state.get("save_execution"):
            from sqlalchemy.orm import Session

            def get_or_create_execution_with_feedbacks(
                session: Session,
                recette_id: str,
                date_selected: date,
                feedbacks: dict[int, str],
            ) -> Execution:
                # exécution
                execution = (
                    session.query(Execution)
                    .filter(
                        Execution.recette_id == recette_id,
                        func.date(Execution.date_execution) == date_selected,
                    )
                    .first()
                )
                if execution is None:
                    print("Création d'une nouvelle exécution")
                    # Création de l’exécution
                    execution = Execution(recette_id=recette_id, date_execution=date_selected)
                    session.add(execution)
                    # session.commit()
                else:
                    print("Exécution existante trouvée, mise à jour des feedbacks")
                    # Si elle existe, on vide ses feedbacks
                    execution.feedbacks.clear()

                # Ajouter les feedbacks
                for convive_id, statut in feedbacks.items():
                    execution.feedbacks.append(FeedbackExecution(convive_id=convive_id, statut=statut))

                return execution

            with get_db_session() as session:
                exec = get_or_create_execution_with_feedbacks(
                    session, recette_id=str(recette.id), date_selected=date_selected, feedbacks=selections
                )

                session.commit()

                st.session_state["save_execution"] = False

                st.rerun()  # Recharge la page pour l’afficher dans les convives


if __name__ == "__main__":
    main()
