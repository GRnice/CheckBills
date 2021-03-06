package com.dg.checkbills.Historique;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.checkbills.Communication.NetworkUtil;
import com.dg.checkbills.Constantes.BroadcastAddr;
import com.dg.checkbills.Data.ArrayUtils;
import com.dg.checkbills.Data.Bill;
import com.dg.checkbills.R;
import com.dg.checkbills.Storage.BillsManager;

import java.util.ArrayList;

/**
 * Created by Remy on 03/11/2016.
 */
public class HistoriqueDetailFragment extends FragmentHistorique
{

    private Bill bill;

    public HistoriqueDetailFragment()
    {
        super();
        this.title = "Detail ticket";
    }

    public void setCheckedBill(Bill bill)
    {
        this.bill = bill;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.frag_historique_detail, container, false);

        TextView title = (TextView) view.findViewById(R.id.titleDetailHistoriqueModif);
        TextView typeBill = (TextView) view.findViewById(R.id.textViewType);
        TextView date = (TextView) view.findViewById(R.id.dateDetailHistorique);
        TextView montant = (TextView) view.findViewById(R.id.montantEditDetailHistorique);
        Button modification = (Button) view.findViewById(R.id.buttonmodifhist);

        modification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                HistoriqueModifFragment hmf = HistoriqueModifFragment.newInstance(bill);
                ((HistoriqueActivity)getActivity()).setFragment(hmf);
                return;
            }
        });

        Button delTicket = (Button) view.findViewById(R.id.butttonSupprimerTicket);
        delTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (NetworkUtil.getConnectivityStatus(getActivity()) == 0)
                {
                    Toast.makeText(getActivity(),"Activez internet",Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intentDel = new Intent();
                intentDel.setAction(BroadcastAddr.ACTION_TO_SERVICE_FROM_ACTIVITY.getAddr());
                intentDel.putExtra("REQUEST-SUPPR",bill.getId());
                getActivity().sendBroadcast(intentDel);
            }
        });

        title.setText(this.bill.getNom());

        switch(this.bill.getType())
        {
            case "1":
            {typeBill.setText("Categorie : Alimentaire");
                break;}
            case "2":
            {typeBill.setText("Categorie : Professionnel");
                break;}
            case "3":
            {typeBill.setText("Categorie : Loisir");
                break;}
            case "4":
            {typeBill.setText("Categorie : Voyage");
                break;}
        }
        date.setText(this.bill.getDate());

        montant.setText("Montant : "+String.valueOf(this.bill.getMontant())+"€");
        return view;
    }

    public void setImage(ArrayList<Byte> arrayimage)
    {
        Log.e("Episode 5","requestImage");
        ImageView imgview = (ImageView) getView().findViewById(R.id.imageTicketDetailModif);
        byte[] array = ArrayUtils.toPrimitive(arrayimage);

        Bitmap bmp = BitmapFactory.decodeByteArray(array,0,array.length);
        imgview.setImageBitmap(bmp);
    }
}
